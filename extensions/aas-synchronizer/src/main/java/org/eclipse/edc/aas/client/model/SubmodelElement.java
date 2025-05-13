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

import java.util.ArrayList;
import java.util.List;

public class SubmodelElement extends Referrable {
    private final List<Qualifier> qualifiers = new ArrayList<>();
    private Reference semanticId;
    private String kind;

    private SubmodelElement() {
    }

    public List<Qualifier> getQualifiers() {
        return qualifiers;
    }

    public Reference getSemanticId() {
        return semanticId;
    }

    public String getKind() {
        return kind;
    }

}
