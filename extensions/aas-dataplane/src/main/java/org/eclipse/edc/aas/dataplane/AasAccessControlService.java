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

import jakarta.ws.rs.core.UriInfo;
import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAccessControlService;
import org.eclipse.edc.spi.iam.ClaimToken;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.Base64;
import java.util.Map;

public class AasAccessControlService implements DataPlaneAccessControlService {
    @Override
    public Result<Void> checkAccess(ClaimToken claimToken, DataAddress sourceDataAddress, Map<String, Object> requestData, Map<String, Object> additionalData) {

        if (requestData.get("path") instanceof UriInfo uriInfo) {
            var path = uriInfo.getPathSegments();
            if (path.isEmpty()) {
                return Result.failure("Access Denied - public API request must have the base64-encoded AAS ID in the path");
            }

            var aasId = new String(Base64.getUrlDecoder().decode(path.get(0).getPath()));

            if (path.size() > 1) {
                var submodelName = path.get(1).getPath();
                //todo: check if the submodelName is valid
                sourceDataAddress.getProperties().put("submodelElement", submodelName);
            }

            return aasId.equals(sourceDataAddress.getStringProperty("id")) ? Result.success() : Result.failure("Access Denied");
        }
        return Result.failure("Access Denied - public API request must have the base64-encoded AAS ID in the path");
    }
}
