terraform {
  required_providers {
    volcengine = {
      source = "volcengine/volcengine"
      version = "0.0.174"
    }
  }
}

variable "access_key" {
  description = "火山引擎访密钥"
  type        = string
}

variable "secret_key" {
  description = "火山引擎访密钥"
  type        = string
}

provider "volcengine" {
  access_key = var.access_key
  secret_key = var.secret_key
  region = "cn-guangzhou"
}

resource "volcengine_vke_cluster" "vke-demo" {
  name                      = "vke-demo"
  description               = ""
  delete_protection_enabled = false
  cluster_config {
    subnet_ids                       = ["subnet-3ezazxgeqvncw72200ryjhddm"]
    api_server_public_access_enabled = true
    api_server_public_access_config {
      public_access_network_config {
        billing_type = "PostPaidByBandwidth"
        bandwidth    = 1
      }
    }
    resource_public_access_default_enabled = true
  }
  pods_config {
    pod_network_mode = "VpcCniShared"
    vpc_cni_config {
      subnet_ids = ["subnet-3ezazxgeqvncw72200ryjhddm"]
    }
  }
  services_config {
    service_cidrsv4 = ["192.168.192.0/18"]
  }
  project_name = "default"
  tags {
    key   = "project"
    value = "demo"
  }
}

resource "volcengine_vke_kubeconfig" "kube_config" {
  cluster_id     = volcengine_vke_cluster.vke-demo.id
  type           = "Public"
  valid_duration = 2
}

resource "volcengine_vke_node_pool" "vke-demo-ng1" {
  cluster_id = volcengine_vke_cluster.vke-demo.id
  name       = "ng1"
  auto_scaling {
    enabled          = true
    min_replicas     = 0
    max_replicas     = 5
    desired_replicas = 4 
    priority         = 5
    subnet_policy    = "ZoneBalance"
  }
  node_config {
    instance_type_ids = ["ecs.g3il.xlarge"]
    subnet_ids = ["subnet-3ezazxgeqvncw72200ryjhddm"]
    image_id = "image-ybqd474ge1ftrj6h7ok7"
    system_volume {
      type = "ESSD_PL0"
      size = 80
    }
    data_volumes {
      type        = "ESSD_PL0"
      size        = 80
      mount_point = "/tf1"
    }
    data_volumes {
      type        = "ESSD_PL0"
      size        = 60
      mount_point = "/tf2"
    }
    security {
      login {
        ssh_key_pair_name = "guangzhou-1"
      }
    }
    additional_container_storage_enabled = false
    instance_charge_type                 = "PostPaid"
    name_prefix                          = "vke-demo-ng"
    project_name                         = "default"
    ecs_tags {
      key   = "project"
      value = "demo"
    }
  }
  kubernetes_config {
    labels {
      key   = "project"
      value = "demo"
    }
    cordon             = false 
    auto_sync_disabled = false
  }
  tags {
    key   = "project"
    value = "demo"
  }
}

resource "volcengine_vke_permission" "vke-demo-permission" {
  role_name    = "vke:admin"
  grantee_id   = 54262541
  grantee_type = "User"
  role_domain  = "cluster"
  cluster_id   = volcengine_vke_cluster.vke-demo.id
}

resource "volcengine_vke_addon" "foo" {
  cluster_id = volcengine_vke_cluster.vke-demo.id 
  name = "core-dns"
  version = "1.11.1"
  deploy_node_type = "Node"
  deploy_mode = "Unmanaged"
  depends_on = [volcengine_vke_node_pool.vke-demo-ng1]
  config = "{\"Resources\":{\"Requests\":{\"Cpu\":\"0.2\", \"Memory\":\"512Mi\"}}}"
}

output "kube_config" {
  value = volcengine_vke_cluster.vke-demo.kubeconfig_public
}


