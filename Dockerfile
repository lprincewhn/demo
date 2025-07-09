# Use openjdk:8-jdk-alpine as the base image
FROM openjdk:8-jdk-alpine

# Install bash package
RUN apk add --no-cache bash
