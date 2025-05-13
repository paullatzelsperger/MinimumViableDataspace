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
import okhttp3.Credentials;
import okhttp3.Request;
import org.eclipse.edc.aas.client.model.Submodel;
import org.eclipse.edc.aas.client.model.SubmodelResponse;
import org.eclipse.edc.http.spi.EdcHttpClient;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class AasClientImpl implements AasClient {
    private final EdcHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String username;
    private final String password;

    public AasClientImpl(EdcHttpClient httpClient, ObjectMapper objectMapper, String baseUrl, String username, String password) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public List<Submodel> getSubmodels() {
        var request = new Request.Builder()
                .header("Authorization", Credentials.basic(username, password))
                .url(baseUrl + "/submodels")
                .get()
                .build();

        try (var response = httpClient.execute(request)) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch submodels: " + response.message());
            }
            return objectMapper.readValue(requireNonNull(response.body()).byteStream(), SubmodelResponse.class).result();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching submodels", e);
        }

    }
}
