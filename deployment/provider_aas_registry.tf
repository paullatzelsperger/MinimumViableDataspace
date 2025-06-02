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

          # uncomment this if automatically mapping the OPC UA NodeSets is desired

          # volume_mount {
          #   name       = "nodeset-volume"
          #   mount_path = "/app/NodeSets"
          # }
        }

        # volume {
        #   name = "nodeset-volume"
        #   config_map {
        #     name = kubernetes_config_map.provider_aas_nodeset.metadata[0].name
        #   }
        # }
      }
    }
  }
}

resource "kubernetes_service" "provider_aas_registry_service" {
  metadata {
    name      = "provider-aas-registry"
    namespace = kubernetes_namespace.ns.metadata.0.name
  }

  spec {
    selector = {
      App = kubernetes_deployment.provider_aas_registry.metadata[0].labels.App
    }

    port {
      name        = "api"
      port        = 8080
      target_port = 8080
    }

    type = "ClusterIP"
  }
}

# uncomment this if automatically mapping the OPC UA NodeSets is desired

# resource "kubernetes_config_map" "provider_aas_nodeset" {
#   metadata {
#     name      = "provider-aas-nodeset"
#     namespace = kubernetes_namespace.ns.metadata.0.name
#   }
#
#   data = {
#     for file in fileset("${path.root}/assets/provider/NodeSets", "*") :
#     file => file("${path.root}/assets/provider/NodeSets/${file}")
#   }
# }


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

# this ingress exposes the AAS Repository API to the outside world. Most notably, we'll use it to seed the node set file
# in the seed script.
resource "kubernetes_ingress_v1" "provider_aas_ingress" {
  metadata {
    name      = "provider-aas-ingress"
    namespace = kubernetes_namespace.ns.metadata[0].name
    annotations = {
      # Use NGINX ingress controller (adjust if using a different one)
      "nginx.ingress.kubernetes.io/rewrite-target" = "/$2"
    }
  }

  spec {
    ingress_class_name = "nginx" # or "public" / "nginx-internal" based on your controller

    rule {

      http {
        path {
          path = "/aas(/|$)(.*)"

          backend {
            service {
              name = kubernetes_service.provider_aas_registry_service.metadata[0].name
              port {
                number = 8080
              }
            }
          }
        }
      }
    }
  }
}
