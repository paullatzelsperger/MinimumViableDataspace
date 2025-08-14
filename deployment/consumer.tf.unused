#
#  Copyright (c) 2024 Metaform Systems, Inc.
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0
#
#  SPDX-License-Identifier: Apache-2.0
#
#  Contributors:
#       Metaform Systems, Inc. - initial API and implementation
#

# This file deploys all the components needed for the consumer side of the scenario,
# i.e. the connector, an identityhub and a vault.

# consumer connector
module "consumer-connector" {
  source            = "./modules/connector"
  humanReadableName = "consumer"
  participantId     = var.consumer-did
  database = {
    user     = "consumer"
    password = "consumer"
    url      = "jdbc:postgresql://${module.consumer-postgres.database-url}/consumer"
  }
  vault-url           = "http://consumer-vault.${kubernetes_namespace.ns-consumer-ctrl.metadata.0.name}.svc.cluster.local:8200"
  namespace           = kubernetes_namespace.ns-consumer-ctrl.metadata.0.name
  namespace-dataplane = kubernetes_namespace.ns-consumer-data.metadata.0.name
  sts-token-url       = "${module.consumer-identityhub.sts-token-url}/token"
  useSVE              = var.useSVE
  image-pull-policy = var.pull-policy

}

# Postgres database for the consumer connector
module "consumer-postgres" {
  depends_on       = [kubernetes_config_map.postgres-initdb-config-consumer]
  source           = "./modules/postgres"
  instance-name    = "consumer"
  init-sql-configs = ["consumer-initdb-config"]
  namespace        = kubernetes_namespace.ns-consumer-ctrl.metadata.0.name
}

# consumer identity hub
module "consumer-identityhub" {
  depends_on        = [module.consumer-identityhub-vault]
  source            = "./modules/identity-hub"
  credentials-dir   = dirname("./assets/credentials/k8s/consumer/")
  humanReadableName = "consumer-identityhub"
  participantId     = var.consumer-did
  vault-url         = "http://consumer-identityhub-vault.${kubernetes_namespace.ns-consumer-security.metadata.0.name}.svc.cluster.local:8200"
  service-name      = "consumer"
  database = {
    user     = "consumer"
    password = "consumer"
    url      = "jdbc:postgresql://${module.consumer-identityhub-postgres.database-url}/consumer"
  }
  namespace = kubernetes_namespace.ns-consumer-security.metadata.0.name
  useSVE    = var.useSVE
  image-pull-policy = var.pull-policy

}

# Postgres database for the consumer identity hub
module "consumer-identityhub-postgres" {
  depends_on       = [kubernetes_config_map.postgres-ih-initdb-config-consumer]
  source           = "./modules/postgres"
  instance-name    = "consumer-identityhub"
  init-sql-configs = ["consumer-ih-initdb-config"]
  namespace        = kubernetes_namespace.ns-consumer-security.metadata.0.name
}


# consumer vault
module "consumer-vault" {
  source            = "./modules/vault"
  humanReadableName = "consumer-vault"
  namespace         = kubernetes_namespace.ns-consumer-ctrl.metadata.0.name
}

# consumer identityhub vault
module "consumer-identityhub-vault" {
  source            = "./modules/vault"
  humanReadableName = "consumer-identityhub-vault"
  namespace         = kubernetes_namespace.ns-consumer-security.metadata.0.name
}


# DB initialization for the EDC database
resource "kubernetes_config_map" "postgres-initdb-config-consumer" {
  metadata {
    name      = "consumer-initdb-config"
    namespace = kubernetes_namespace.ns-consumer-ctrl.metadata.0.name
  }
  data = {
    "consumer-initdb-config.sql" = <<-EOT
        CREATE USER consumer WITH ENCRYPTED PASSWORD 'consumer' SUPERUSER;
        CREATE DATABASE consumer;
        \c consumer consumer


      EOT
  }
}

# DB initialization for the Identity Hub database
resource "kubernetes_config_map" "postgres-ih-initdb-config-consumer" {
  metadata {
    name      = "consumer-ih-initdb-config"
    namespace = kubernetes_namespace.ns-consumer-security.metadata.0.name
  }
  data = {
    "consumer-ih-initdb-config.sql" = <<-EOT
        CREATE USER consumer WITH ENCRYPTED PASSWORD 'consumer' SUPERUSER;
        CREATE DATABASE consumer;
        \c consumer consumer


      EOT
  }
}


resource "kubernetes_namespace" "ns-consumer-ctrl" {
  metadata {
    name = "mvd-consumer-ctrl"
  }
}

resource "kubernetes_namespace" "ns-consumer-security" {
  metadata {
    name = "mvd-consumer-security"
  }
}

resource "kubernetes_namespace" "ns-consumer-data" {
  metadata {
    name = "mvd-consumer-data"
  }
}
