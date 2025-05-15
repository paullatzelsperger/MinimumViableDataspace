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


import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.eclipse.edc.connector.dataplane.http.pipeline.HttpPart;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult;
import org.eclipse.edc.http.spi.EdcHttpClient;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.eclipse.edc.connector.dataplane.spi.pipeline.StreamResult.error;

public final class AasDataSource implements DataSource {
    private final EdcHttpClient httpClient;
    private final String id;
    @Nullable
    private final String submodelElement;
    private final String name;
    private final String baseUrl;
    private final AtomicReference<ResponseBodyStream> responseBodyStream = new AtomicReference<>();
    private final Monitor monitor;

    public AasDataSource(EdcHttpClient httpClient, String id, @Nullable String submodelElement, String name, String baseUrl, Monitor monitor) {
        this.httpClient = httpClient;
        this.id = id;
        this.submodelElement = submodelElement;
        this.name = name;
        this.baseUrl = baseUrl;
        this.monitor = monitor;
    }

    @Override
    public StreamResult<Stream<Part>> openPartStream() {

        var submodelPath = Base64.getUrlEncoder().encodeToString(id.getBytes());

        if (submodelElement != null) {
            submodelPath += "/submodel-elements/" + submodelElement;
        }
        var request = new Request.Builder()
                .addHeader("Authorization", Credentials.basic("admin", "pwd"))
                .url(baseUrl + "/" + submodelPath)
                .get()
                .build();

        try {
            var response = httpClient.execute(request);
            if (response.isSuccessful()) {
                var body = response.body();
                if (body == null) {
                    throw new EdcException(format("Received empty response body transferring submodel data from AAS Server: %s", response.code()));
                }
                var stream = body.byteStream();

                return StreamResult.success(Stream.of(new HttpPart(name, stream, "application/json")));
            } else {
                try {
                    if (401 == response.code() || 403 == response.code()) {
                        return StreamResult.notAuthorized();
                    } else if (404 == response.code()) {
                        return StreamResult.notFound();
                    } else {
                        return error(format("Received code transferring HTTP data: %s - %s.", response.code(), response.message()));
                    }
                } finally {
                    try {
                        response.close();
                    } catch (Exception e) {
                        monitor.severe("Error closing failed response", e);
                    }
                }
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public void close() throws Exception {
        var bodyStream = responseBodyStream.get();
        if (bodyStream != null) {
            bodyStream.responseBody().close();
            try {
                bodyStream.stream().close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public EdcHttpClient httpClient() {
        return httpClient;
    }

    public String name() {
        return name;
    }

    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AasDataSource) obj;
        return Objects.equals(this.httpClient, that.httpClient) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpClient, name, baseUrl);
    }

    @Override
    public String toString() {
        return "AasDataSource[" +
                "httpClient=" + httpClient + ", " +
                "name=" + name + ", " +
                "baseUrl=" + baseUrl + ']';
    }


    private record ResponseBodyStream(ResponseBody responseBody, InputStream stream) {

    }
}