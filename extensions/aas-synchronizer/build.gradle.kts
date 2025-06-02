/*
*  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
*
*  This program and the accompanying materials are made available under the
*  terms of the Apache License, Version 2.0 which is available at
*  https://www.apache.org/licenses/LICENSE-2.0
*
*  SPDX-License-Identifier: Apache-2.0
*
*  Contributors:
*       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - Initial API and Implementation
*
*/

plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.spi.http)
    implementation(libs.edc.lib.api)
    implementation(libs.edc.lib.http)
    implementation(libs.edc.api.asset)
    testImplementation(libs.edc.junit)
    testImplementation(libs.testcontainers.junit)
}
