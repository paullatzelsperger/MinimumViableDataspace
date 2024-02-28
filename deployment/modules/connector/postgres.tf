#
#  Copyright (c) 2024 Contributors to the Eclipse Foundation
#
#  See the NOTICE file(s) distributed with this work for additional
#  information regarding copyright ownership.
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations
#  under the License.
#
#  SPDX-License-Identifier: Apache-2.0
#

resource "kubernetes_deployment" "postgres" {
  metadata {
    name      = "${var.humanReadableName}-postgres"
    namespace = var.namespace
    labels    = {
      App = "postgres"
    }
  }

  spec {
    replicas = 1
    selector {
      match_labels = {
        App = "postgres"
      }
    }
    template {
      metadata {
        labels = {
          App = "postgres"
        }
      }
      spec {
        container {
          image = local.pg-image
          name  = "postgres"

          env_from {
            config_map_ref {
              name = kubernetes_config_map.postgres-config.metadata[0].name
            }
          }
          port {
            container_port = var.ports.postgres
            name           = "postgres-port"
          }

          volume_mount {
            mount_path = "/docker-entrypoint-initdb.d/"
            name       = "pg-initdb"
          }

          # Uncomment this to assign (more) resources
          resources {
            limits = {
              cpu    = "2"
              memory = "512Mi"
            }
            requests = {
              cpu    = "250m"
              memory = "50Mi"
            }
          }
          liveness_probe {
            exec {
              command = ["pg_isready", "-U", "postgres"]
            }
            failure_threshold = 10
            period_seconds    = 5
            timeout_seconds   = 30
          }
        }
        volume {
          name = "pg-initdb"
          config_map {
            name = kubernetes_config_map.postgres-config.metadata.0.name
          }
        }
      }
    }
  }
}

# ConfigMap that contains SQL statements to initialize the DB, create a "miw" DB, etc.
resource "kubernetes_config_map" "postgres-config" {
  metadata {
    name = "pg-initdb-config-${var.humanReadableName}"
    namespace = var.namespace
  }

  ## Create databases for keycloak and MIW, create users and assign privileges
  data = {
    POSTGRES_USER     = "postgres"
    POSTGRES_PASSWORD = "postgres"
    "init.sql"        = <<EOT
      CREATE DATABASE identityhub;
    EOT
  }
}

# K8S ClusterIP so Keycloak and MIW can access postgres
resource "kubernetes_service" "pg-service" {
  metadata {
    name = "postgres-service-${var.humanReadableName}"
    namespace = var.namespace
  }
  spec {
    selector = {
      App = kubernetes_deployment.postgres.spec.0.template.0.metadata[0].labels.App
    }
    port {
      name        = "pg-port"
      port        = var.ports.postgres
      target_port = var.ports.postgres
    }
  }
}

locals {
  pg-image   = "postgres:16-alpine"
  pg-ip      = kubernetes_service.pg-service.spec.0.cluster_ip
  pg-host    = "${local.pg-ip}:${var.ports.postgres}"
}
