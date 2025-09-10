terraform {
  required_providers {
    kubernetes = {
      source = "hashicorp/kubernetes"
      version = "2.37.1"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "cd1vm6vvmmlrc2ho39agg@54262541-kd1vniqo6v1be8j2vt53g"
}

# 定义多个主域名变量，允许用户传入多个自定义域名
variable "primary_domains" {
  description = "主域名列表，如[\"svhw.tech\", \"example.com\"]"
  type        = list(string)
  default     = ["svhw.tech"] # 默认值，可在调用时覆盖
}

# 创建Kubernetes Ingress资源
resource "kubernetes_ingress_v1" "kube_dns_alb_ingress_http" {
  metadata {
    name      = "kube-dns-alb-ingress-http"
    namespace = "kube-system"
    
    annotations = {
      "ingress.vke.volcengine.com/loadbalancer-port"        = "80"
      "ingress.vke.volcengine.com/loadbalancer-protocol"    = "http"
      "ingress.vke.volcengine.com/loadbalancer-pass-through" = "true"
      "ingress.vke.volcengine.com/loadbalancer-rules-configs" = jsonencode(flatten([
        for domain in var.primary_domains : [
          {
            "host"        = domain
            "path"        = "/"
            "ruleAction"  = "Redirect"
            "redirectConfig" = {
              "port"     = 443
              "httpCode" = "302"
              "protocol" = "HTTPS"
            }
          },
          {
            "host"        = "*.${domain}"
            "path"        = "/"
            "ruleAction"  = "Redirect"
            "redirectConfig" = {
              "port"     = 443
              "httpCode" = "302"
              "protocol" = "HTTPS"
            }
          }
        ]
      ]))
    }
  }
  spec {
    ingress_class_name = "demo-alb"
    
    # 为每个主域名创建主域名规则和泛域名规则
    dynamic "rule" {
      for_each = toset(flatten([
        for domain in var.primary_domains : [
          domain,
          "*.${domain}"
        ]
      ]))
      
      content {
        host = rule.value
        
        http {
          path {
            path      = "/"
            path_type = "Prefix"
            
            backend {
              service {
                name = "redirect"
                port {
                  name = "use-annotation"
                }
              }
            }
          }
        }
      }
    }
  }
}
