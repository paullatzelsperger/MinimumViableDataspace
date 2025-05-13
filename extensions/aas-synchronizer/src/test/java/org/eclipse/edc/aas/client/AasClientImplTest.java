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

package org.eclipse.edc.aas.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import org.eclipse.edc.http.client.EdcHttpClientImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

//todo: convert to testcontainer test using the official AAS repo image
class AasClientImplTest {

    private final EdcHttpClientImpl httpClient = new EdcHttpClientImpl(new OkHttpClient(), RetryPolicy.ofDefaults(), mock());
    private final AasClientImpl client = new AasClientImpl(httpClient, new ObjectMapper(), "http://localhost:8081/api/v3.0", "admin", "pwd");


    @Test
    void getSubmodels() {
        assertThat(client.getSubmodels()).hasSize(7);
    }

}