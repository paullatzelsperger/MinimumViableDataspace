#
#  Copyright (c) 2025 Metaform Systems Inc.
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0
#
#  SPDX-License-Identifier: Apache-2.0
#
#  Contributors:
#       Metaform Systems Inc. - initial API and implementation
#

resource "kubernetes_deployment" "provider_aas_registry" {
  metadata {
    name      = "provider-aas-registry"
    namespace = kubernetes_namespace.ns.metadata.0.name
    labels = {
      App = "provider-aas-registry"
    }
  }

  spec {
    replicas = 1
    selector {
      match_labels = {
        App = "provider-aas-registry"
      }
    }

    template {
      metadata {
        labels = {
          App = "provider-aas-registry"
        }
      }

      spec {
        container {
          image_pull_policy = "IfNotPresent"
          image             = "ghcr.io/digitaltwinconsortium/aas-repository:nightly"
          name              = "dtc-aas-repository"

          port {
            container_port = "8080"
            name           = "api"
          }

          env {
            name = "ServicePassword"
            value_from {
              secret_key_ref {
                name = "provider-aas-secret"
                key  = "ServicePassword"
              }
            }
          }
        }
      }
    }
  }
}


resource "kubernetes_secret" "provider_aas_secret" {
  metadata {
    name      = "provider-aas-secret"
    namespace = kubernetes_namespace.ns.metadata.0.name
  }

  data = {
    ServicePassword = var.provider-aas-api-password
  }
  type = "Opaque"
}