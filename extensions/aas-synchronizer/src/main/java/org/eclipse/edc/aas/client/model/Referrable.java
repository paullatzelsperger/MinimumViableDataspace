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

import java.util.Collection;

public abstract class Referrable {
    private String category;
    private Collection<L18nString> description;
    private Collection<L18nString> displayName;
    private String idShort;
    private String modelType;
    private String checksum;

    public String getDescription(String language) {
        return description.stream()
                .filter(l18nString -> l18nString.language().equals(language))
                .map(L18nString::text)
                .findFirst()
                .orElse(null);
    }

    public String getCategory() {
        return category;
    }

    public Collection<L18nString> getDescription() {
        return description;
    }

    public Collection<L18nString> getDisplayName() {
        return displayName;
    }

    public String getDisplayName(String language) {
        return displayName.stream()
                .filter(l18nString -> l18nString.language().equals(language))
                .map(L18nString::text)
                .findFirst()
                .orElse(null);
    }

    public String getIdShort() {
        return idShort;
    }

    public String getModelType() {
        return modelType;
    }

    public String getChecksum() {
        return checksum;
    }

}
