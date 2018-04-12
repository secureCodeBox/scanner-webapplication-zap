# OWASP Zap Proxy Scanner
# Service Url: http://localhost/zap?parameters=localhost
# --------------------------------
# DOCKER-VERSION 1.8.0
# To build:
# 1. Install docker (http://docker.io)
# 2. Build container: 	docker build -t securebox/zap .
# 3. Run container: 	docker run -it --rm --name zap -p 8080:8080 securebox/zap

FROM owasp/zap2docker-stable
MAINTAINER Robert.Seedorff@iteratec.de

USER root

RUN apt-get update && apt-get upgrade -y && apt-get install -y openjdk-8-jdk supervisor python-pip && apt-get clean
RUN pip install supervisor-stdout

COPY dockerfiles/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY dockerfiles/csrfAuthScript.js /zap/scripts/templates/authentication/csrfAuthScript.js
COPY ./build/libs/secureBoxZap-rest-service-0.4.0-SNAPSHOT.jar /app.jar

VOLUME /tmp
VOLUME /var/log/supervisor

EXPOSE 8080 8090

ENV JAVA_OPTS ""

RUN sh -c 'touch /app.jar'

ENTRYPOINT ["/usr/bin/supervisord"]
CMD []
