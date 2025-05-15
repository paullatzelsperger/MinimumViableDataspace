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

import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessControlService;
import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.validator.spi.DataAddressValidatorRegistry;

import static org.eclipse.edc.aas.dataplane.AasDataPlaneExtension.NAME;

@Extension(value = NAME)
public class AasDataPlaneExtension implements ServiceExtension {
    public static final String NAME = "AAS Dataplane Extension";

    @Inject
    private PipelineService pipelineService;
    @Inject
    private DataAddressValidatorRegistry validatorRegistry;
    @Inject
    private EdcHttpClient edcHttpClient;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var sourceFactory = new AasDataSourceFactory(validatorRegistry, edcHttpClient, context.getMonitor());
        pipelineService.registerFactory(sourceFactory);
    }

    @Provider
    public DataPlaneAccessControlService createAasAuthorizationService() {
        return new AasAccessControlService();
    }
}
