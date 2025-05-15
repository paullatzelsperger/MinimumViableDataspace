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

package org.eclipse.edc.aas.dataplane;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.eclipse.edc.validator.spi.DataAddressValidatorRegistry;
import org.eclipse.edc.validator.spi.ValidationResult;
import org.jetbrains.annotations.NotNull;

public class AasDataSourceFactory implements DataSourceFactory {
    private final DataAddressValidatorRegistry dataAddressValidatorRegistry;
    private final EdcHttpClient edcHttpClient;
    private final Monitor monitor;

    public AasDataSourceFactory(DataAddressValidatorRegistry dataAddressValidatorRegistry, EdcHttpClient edcHttpClient, Monitor monitor) {
        this.dataAddressValidatorRegistry = dataAddressValidatorRegistry;
        this.edcHttpClient = edcHttpClient;
        this.monitor = monitor;
    }

    @Override
    public String supportedType() {
        return "AAS";
    }

    @Override
    public DataSource createSource(DataFlowStartMessage dataFlowStartMessage) {
        var validationResult = validateRequest(dataFlowStartMessage);
        if (validationResult.failed()) {
            throw new EdcException(String.join(", ", validationResult.getFailureMessages()));
        }

        var source = dataFlowStartMessage.getSourceDataAddress();
        return new AasDataSource(edcHttpClient,
                source.getStringProperty("id"),
                source.getStringProperty("submodelElement"),
                source.getStringProperty("displayName"),
                source.getStringProperty("aas_url"), monitor);
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage dataFlowStartMessage) {
        var request = dataFlowStartMessage.getSourceDataAddress();
        return dataAddressValidatorRegistry.validateSource(request).flatMap(ValidationResult::toResult);
    }
}
