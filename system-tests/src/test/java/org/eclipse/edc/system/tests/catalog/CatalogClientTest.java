/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.system.tests.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.edc.catalog.spi.model.FederatedCatalogCacheQuery;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.policy.model.PolicyRegistrationTypes;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.system.tests.utils.TestUtils.requiredPropOrEnv;

@EndToEndTest
class CatalogClientTest {
    static final String CONSUMER_EU_CATALOG_URL = requiredPropOrEnv("CONSUMER_EU_CATALOG_URL", "http://localhost:9192/api/v1/data/federatedcatalog");
    static final String CONSUMER_US_CATALOG_URL = requiredPropOrEnv("CONSUMER_US_CATALOG_URL", "http://localhost:9193/api/v1/data/federatedcatalog");
    static final String NON_RESTRICTED_ASSET_PREFIX = "test-document_";
    static final String RESTRICTED_ASSET_PREFIX = "test-document-2_";
    private static final Duration TEST_POLL_INTERVAL = Duration.ofMillis(250);
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(20);

    static TypeManager typeManager = new TypeManager();

    @BeforeAll
    static void setUp() {
        PolicyRegistrationTypes.TYPES.forEach(typeManager::registerTypes);
    }

    @Test
    void containsOnlyNonRestrictedAsset() {
        await().atMost(TEST_TIMEOUT)
                .pollInterval(TEST_POLL_INTERVAL)
                .untilAsserted(() -> {
                    var nodes = getNodesFromCatalog(CONSUMER_US_CATALOG_URL);
                    assertThat(nodes)
                            .isNotEmpty()
                            .allSatisfy(
                                    n -> assertThat(n.getAsset().getProperty(Asset.PROPERTY_ID)).asString().startsWith(NON_RESTRICTED_ASSET_PREFIX));
                });
    }

    @Test
    void containsAllAssets() {
        await().atMost(TEST_TIMEOUT)
                .pollInterval(TEST_POLL_INTERVAL)
                .untilAsserted(() -> {
                    var nodes = getNodesFromCatalog(CONSUMER_EU_CATALOG_URL);
                    assertThat(nodes)
                            .isNotEmpty()
                            .allSatisfy(
                                    n -> assertThat(n.getAsset().getId()).asString()
                                            .satisfiesAnyOf(
                                                    s -> assertThat(s).startsWith(NON_RESTRICTED_ASSET_PREFIX),
                                                    s -> assertThat(s).startsWith(RESTRICTED_ASSET_PREFIX)));
                });
    }

    private List<ContractOffer> getNodesFromCatalog(String consumerCatalogUrl) {
        var nodesJson = given()
                .contentType("application/json")
                .header("X-Api-Key", "ApiKeyDefaultValue")
                .body(FederatedCatalogCacheQuery.Builder.newInstance().build())
                .when()
                .post(consumerCatalogUrl)
                .then()
                .statusCode(200)
                .extract().body().asString();
        return typeManager.readValue(nodesJson, new TypeReference<>() {
        });
    }
}
