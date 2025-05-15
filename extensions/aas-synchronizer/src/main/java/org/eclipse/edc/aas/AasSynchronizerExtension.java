/*
 *  Copyright (c) 2025 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.edc.aas;

import org.eclipse.edc.aas.client.AasClient;
import org.eclipse.edc.aas.client.AasClientImpl;
import org.eclipse.edc.aas.client.model.Submodel;
import org.eclipse.edc.aas.client.model.SubmodelAssetBuilder;
import org.eclipse.edc.aas.validation.AasDataAddressValidator;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.connector.controlplane.services.spi.contractdefinition.ContractDefinitionService;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.validator.spi.DataAddressValidatorRegistry;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.edc.aas.AasSynchronizerExtension.NAME;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

@Extension(value = NAME)
public class AasSynchronizerExtension implements ServiceExtension {
    public static final String NAME = "AAS Synchronizer Extension";
    private ScheduledExecutorService executor;
    private AasClient aasClient;
    @Inject
    private EdcHttpClient httpClient;
    @Inject
    private TypeManager typeManager;
    @Setting(key = "mvd.aas.server.url", description = "Base URL of the AAS server/repository. Must include the host, port and API base path (usually '/api/v3.0/')", required = false)
    private String baseUrl;
    @Setting(key = "mvd.aas.server.user", description = "Username for the AAS server", required = false)
    private String aasUsername;
    @Setting(key = "mvd.aas.server.password", description = "Password for the AAS server", required = false)
    private String aasPassword;
    @Setting(key = "mvd.aas.server.sync.initialDelay", description = "Initial delay for the AAS synchronization task in seconds", defaultValue = "5")
    private int initialDelay;
    @Setting(key = "mvd.aas.server.sync.period", description = "Period for the AAS synchronization task in seconds", defaultValue = "60")
    private int syncPeriod;
    @Setting(key = "mvd.aas.server.sync.definitionId", description = "ID of the contract definition to be used for the assets synchronized from AAS", defaultValue = "mvd:aas:sync:contractdefinition:1")
    private String contractDefinitionId;
    private Monitor monitor;

    @Inject
    private AssetService index;

    @Inject
    private ContractDefinitionService contractDefinitionService;

    @Inject
    private DataAddressValidatorRegistry dataAddressValidatorRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor().withPrefix(NAME);
        if (baseUrl == null || aasUsername == null || aasPassword == null) {
            monitor.warning("Base URL, AAS API username and AAS API password are required. This runtime will not synchronize assets from the AAS server.");
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        aasClient = new AasClientImpl(httpClient, typeManager.getMapper(), baseUrl, aasUsername, aasPassword);
        dataAddressValidatorRegistry.registerSourceValidator("AAS", new AasDataAddressValidator(monitor));

    }

    @Override
    public void start() {
        if (executor != null) {
            executor.scheduleAtFixedRate(() -> {
                var result = aasClient.getSubmodels();
                if (result.failed()) {
                    monitor.warning("Failed to synchronize assets from the AAS server: %s".formatted(result.getFailureDetail()));
                    return;
                }
                var submodels = result.getContent();
                if (submodels == null || submodels.isEmpty()) {
                    monitor.warning("No AAS submodels found");
                    return;
                }
                monitor.debug("fetched " + submodels.size() + " submodels");


                var assets = submodels.stream().map((Submodel submodel) -> SubmodelAssetBuilder.create(submodel, baseUrl)).toList();
                var any = assets.stream()
                        .map(this::upsert)
                        .filter(ServiceResult::failed)
                        .findAny();

                if (any.isPresent()) {
                    monitor.severe("failed to create asset: " + any.get().getFailureMessages());
                } else {
                    monitor.debug("assets synchronized successfully");

                    // upsert contract definition
                    var assetIds = assets.stream().map(Asset::getId).toList();
                    upsertContractDefinition(assetIds, contractDefinitionId)
                            .onFailure(f -> monitor.severe("failed to create contract definition: " + f.getFailureDetail()))
                            .onSuccess(v -> monitor.debug("contract definition synchronized successfully"));
                }
            }, initialDelay, syncPeriod, SECONDS);
        }
    }

    private ServiceResult<Void> upsertContractDefinition(List<String> assetIds, String contractDefinitionId) {
        var def = contractDefinitionService.findById(contractDefinitionId);
        if (def == null) {
            def = ContractDefinition.Builder.newInstance()
                    .id(contractDefinitionId)
                    .accessPolicyId("require-membership") // todo: make configurable
                    .contractPolicyId("require-dataprocessor")
                    .assetsSelector(List.of(new Criterion(EDC_NAMESPACE + "id", "in", assetIds)))
                    .build();
            return contractDefinitionService.create(def).mapEmpty();
        } else {
            def.getAssetsSelector().clear(); // fixme: not super-clean, should be a copy
            def.getAssetsSelector().add(new Criterion(EDC_NAMESPACE + "id", "in", assetIds));
            return contractDefinitionService.update(def);
        }
    }

    private ServiceResult<Asset> upsert(Asset asset) {
        if (index.findById(asset.getId()) == null) {
            return index.create(asset);
        }
        return index.update(asset);
    }

    @Override
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
