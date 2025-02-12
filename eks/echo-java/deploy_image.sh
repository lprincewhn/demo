#!/bin/bash

mvn clean package
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin 597377428377.dkr.ecr.ap-northeast-1.amazonaws.com
docker build -t 597377428377.dkr.ecr.ap-northeast-1.amazonaws.com/echo-java .
docker push 597377428377.dkr.ecr.ap-northeast-1.amazonaws.com/echo-java
kubectl get pod
eksctl create iamserviceaccount \
    --name echo-ot \
    --cluster eksdemo --region ap-northeast-1 \
    --attach-policy-arn arn:aws:iam::aws:policy/AWSXRayDaemonWriteAccess \
    --attach-policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess \
    --approve \
    --override-existing-serviceaccounts
kubectl rollout restart deployment echo-java-ot
kubectl rollout status deployment echo-java-ot
kubectl get pod -o wide
kubectl logs -f deployment.apps/echo-java-ot
