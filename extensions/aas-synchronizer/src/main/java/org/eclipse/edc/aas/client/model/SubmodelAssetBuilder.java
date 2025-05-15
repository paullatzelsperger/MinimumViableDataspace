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

package org.eclipse.edc.aas.client.model;

import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.spi.types.domain.DataAddress;

public class SubmodelAssetBuilder {

    public static final String DATAADDRESS_AAS_TYPE = "AAS";
    public static final String DATAADDRESS_AAS_URL = "aas_url";
    private static final String KIND = "kind";
    private static final String IDENTIFICATION = "identification";
    private static final String IDENTIFICATION_VALUE = "identification_value";
    private static final String SUBMODEL_ELEMENTS = "submodelElements";
    private static final String DATAADDRESS_AAS_DISPLAYNAME = "displayName";
    private static final String DATAADDRESS_AAS_ID = "id";

    public static Asset create(Submodel submodel, String aasBaseUrl) {
        var asset = Asset.Builder.newInstance()
                .description(submodel.getDescription("en"))
                .name(submodel.getDisplayName("en"))
                .id(submodel.getId())
                .dataAddress(DataAddress.Builder.newInstance()
                        .type(DATAADDRESS_AAS_TYPE)
                        .property(DATAADDRESS_AAS_ID, submodel.getId())
                        .property(DATAADDRESS_AAS_DISPLAYNAME, submodel.getDisplayName("en"))
                        .property(DATAADDRESS_AAS_URL, aasBaseUrl + "/submodels")
                        .build())
                .property(KIND, submodel.getKind())
                .property(IDENTIFICATION, submodel.getIdentification().id())
                .property(IDENTIFICATION_VALUE, submodel.getIdentification().value())
                .property(SUBMODEL_ELEMENTS, submodel.getSubmodelElements());

        return asset.build();
    }
}
