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

package org.eclipse.edc.aas.validation;

import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.validator.spi.ValidationResult;
import org.eclipse.edc.validator.spi.Validator;
import org.eclipse.edc.validator.spi.Violation;

import static org.eclipse.edc.aas.client.model.SubmodelAssetBuilder.DATAADDRESS_AAS_TYPE;
import static org.eclipse.edc.aas.client.model.SubmodelAssetBuilder.DATAADDRESS_AAS_URL;
import static org.eclipse.edc.validator.spi.ValidationResult.success;

public class AasDataAddressValidator implements Validator<DataAddress> {
    public AasDataAddressValidator(Monitor monitor) {
    }

    @Override
    public ValidationResult validate(DataAddress dataAddress) {
        if (!dataAddress.getType().equals(DATAADDRESS_AAS_TYPE)) {
            return ValidationResult.failure(Violation.violation("Expected DataAddress type: %s, got %s".formatted(DATAADDRESS_AAS_TYPE, dataAddress.getType()), ".types"));
        }
        if (dataAddress.getStringProperty(DATAADDRESS_AAS_URL) == null) {
            return ValidationResult.failure(Violation.violation("Missing required property: %s".formatted(DATAADDRESS_AAS_URL), ".properties"));
        }

        return success();
    }
}
