FROM amazoncorretto:17-alpine
#FROM amazoncorretto:21-al2-jdk
#FROM adoptopenjdk:8-jdk-openj9
WORKDIR /app

COPY jmx-agent/jmx_prometheus_javaagent-0.17.0.jar /app/jmx_prometheus_javaagent.jar
COPY jmx-agent/jmx_config.yaml /app/jmx_config.yaml
ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/download/v1.30.0/aws-opentelemetry-agent.jar /app/aws-opentelemetry-agent.jar
#COPY otel-agent/aws-opentelemetry-agent.jar /app/aws-opentelemetry-agent.jar
RUN mkdir -p disco/disco-plugins
COPY xray-agent/disco/disco-java-agent.jar ./disco/disco-java-agent.jar
COPY xray-agent/disco/disco-plugins/* ./disco/disco-plugins

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ./app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
