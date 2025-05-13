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

public abstract class Identifiable extends Referrable {
    private Identification identification;
    private String id;
    private AdministrativeInfo administration;

    public Identification getIdentification() {
        return identification;
    }

    public String getId() {
        return id;
    }

    public AdministrativeInfo getAdministration() {
        return administration;
    }

}
