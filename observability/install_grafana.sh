helm repo add grafana https://grafana.github.io/helm-charts
helm install grafana grafana/grafana \
    --namespace prometheus \
    --set persistence.storageClassName="gp2" \
    --set persistence.enabled=true \
    --set adminPassword='Admin!234' \
    --values grafana.yaml \
    --set service.type=LoadBalancer \
    --set-json service.annotations='{"service.beta.kubernetes.io/aws-load-balancer-internal":"true"}'
