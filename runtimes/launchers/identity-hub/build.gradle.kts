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
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.edc.ih.api)
    runtimeOnly(libs.edc.ih.credentials)
    runtimeOnly(libs.edc.ih.participants)
    runtimeOnly(libs.edc.ih.keypairs)

    // SQL modules
    runtimeOnly(libs.edc.transaction.local)
    runtimeOnly(libs.edc.sql.pool)
    runtimeOnly(libs.edc.ih.sql.credentialstore)
    runtimeOnly(libs.edc.ih.sql.didstore)
    runtimeOnly(libs.edc.ih.sql.keypairstore)
    runtimeOnly(libs.edc.ih.sql.participantcontextstore)
    runtimeOnly(libs.postgres)


    runtimeOnly(libs.bundles.management.api)
    implementation(libs.bundles.did)
    implementation(project(":extensions:common-mocks"))
    implementation(libs.bundles.connector)
    implementation(libs.edc.ih.spi.store)
    implementation(libs.edc.identity.vc.ldp)
    implementation(libs.edc.ih.credentials)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("identity-hub.jar")
}

edcBuild {
    publish.set(false)
}
