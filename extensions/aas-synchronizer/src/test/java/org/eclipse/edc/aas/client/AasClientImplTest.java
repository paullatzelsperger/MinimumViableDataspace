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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.edc.http.client.EdcHttpClientImpl;
import org.eclipse.edc.junit.assertions.AbstractResultAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.junit.testfixtures.TestUtils.getFileFromResourceName;
import static org.mockito.Mockito.mock;

//todo: convert to testcontainer test using the official AAS repo image
@Testcontainers
class AasClientImplTest {

    @Container
    private static final GenericContainer<?> CONTAINER = new GenericContainer<>("ghcr.io/digitaltwinconsortium/aas-repository:nightly")
            .withExposedPorts(8080)
            .withEnv(Map.of("ServicePassword", "pwd"))
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*OPC UA server started.*")
                    .withTimes(1)
                    .withStartupTimeout(java.time.Duration.ofSeconds(20)));

    private final EdcHttpClientImpl httpClient = new EdcHttpClientImpl(new OkHttpClient(), RetryPolicy.ofDefaults(), mock());
    private AasClientImpl client;

    @BeforeEach
    void setup() {
        var port = CONTAINER.getMappedPort(8080);
        client = new AasClientImpl(httpClient, new ObjectMapper(), "http://localhost:%s/api/v3.0".formatted(port), "admin", "pwd");

        // upload the Digital Battery Passport nodeset. Mapping a file to the container does not work due to file permissions
        var file = getFileFromResourceName("DigitalBatteryPassport.NodeSet2.xml");
        var fileBody = RequestBody.create(file, MediaType.parse("text/xml"));

        var requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("files", file.getName(), fileBody)
                .addFormDataPart("autodownloadreferences", "false")
                .build();

        var request = new Request.Builder()
                .url("http://localhost:%s/Browser/LocalFileOpen".formatted(port))
                .post(requestBody)
                .build();

        try (var response = httpClient.execute(request)) {
            assertThat(response.code()).isEqualTo(200);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

    }

    @Test
    void getSubmodels() {
        AbstractResultAssert.assertThat(client.getSubmodels()).isSucceeded()
                .satisfies(l -> assertThat(l).hasSize(7));
    }

}