#!/bin/bash

aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 597377428377.dkr.ecr.us-east-2.amazonaws.com
docker build -t 597377428377.dkr.ecr.us-east-2.amazonaws.com/echo-python .
docker push 597377428377.dkr.ecr.us-east-2.amazonaws.com/echo-python
kubectl get pod
