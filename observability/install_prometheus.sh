helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/prometheus \
    --namespace prometheus \
    --values prometheus.yaml \
    --set alertmanager.persistentVolume.storageClass="gp2" \
    --set server.persistentVolume.storageClass="gp2" \
    --set server.service.type=LoadBalancer \
    --set-json service.annotations='{"service.beta.kubernetes.io/aws-load-balancer-internal":"true"}'
