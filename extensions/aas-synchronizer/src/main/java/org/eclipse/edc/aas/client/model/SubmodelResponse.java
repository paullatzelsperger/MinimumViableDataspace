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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SubmodelResponse(@JsonProperty("result") List<Submodel> submodels,
                               @JsonProperty("paging_metadata") PagingMetadata pagingMetadata) {

}
