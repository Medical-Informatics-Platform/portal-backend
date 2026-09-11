#######################################################
# Build the spring boot maven project
#######################################################
FROM maven:3.9.16-amazoncorretto-21 AS mvn-build-env
LABEL maintainer="Thanasis Karampatsis <tkarabatsis@athenarc.gr>"

ENV CODE_PATH="/opt/code"
WORKDIR $CODE_PATH

COPY pom.xml $CODE_PATH/

# Pre-fetch dependencies first to improve build cache efficiency.
RUN mvn -B -ntp dependency:go-offline

COPY src/ $CODE_PATH/src

RUN mvn -B -ntp clean package

#######################################################
# Setup the running container
#######################################################
FROM amazoncorretto:21-alpine3.24

#######################################################
# Setting up timezone
#######################################################
ENV TZ=Etc/GMT
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#######################################################
# Setting up environment
#######################################################
ENV SERVICE="platform-backend"
ENV FEDERATION="default"
ENV LOG_LEVEL="INFO"
ENV FRAMEWORK_LOG_LEVEL="INFO"

WORKDIR /opt

RUN apk add --no-cache curl

#######################################################
# Prepare the spring boot application files
#######################################################
COPY --from=mvn-build-env /opt/code/target/platform-backend.jar /usr/share/jars/

VOLUME /opt/platform/api

RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /opt/config /opt/platform/api \
    && chown -R appuser:appgroup /opt/config /opt/platform/api /usr/share/jars

USER appuser
ENTRYPOINT ["java", "--add-opens", "java.base/java.io=ALL-UNNAMED", "-Daeron.term.buffer.length", "-jar", "/usr/share/jars/platform-backend.jar"]
EXPOSE 8080
HEALTHCHECK --start-period=60s CMD curl --fail --silent --show-error http://localhost:8080/services/actuator/health | grep -q '"status":"UP"'
