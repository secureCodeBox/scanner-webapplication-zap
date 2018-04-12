# OQWASP Zap Proxy Scanner
# Service Url: http://localhost/zap?parameters=localhost
# --------------------------------
# DOCKER-VERSION 1.8.0
# To build:
# 1. Install docker (http://docker.io)
# 2. Build container: 	docker build -t securebox/zap-integrationtest -f Dockerfile.integrationtest
# 3. Run container: 	docker run -it --rm --name zap securebox/zap-integrationtest

FROM docker.iteratec.de:5000/securebox/zap:develop
MAINTAINER Robert.Seedorff@iteratec.de

VOLUME /develop
WORKDIR /develop

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH $JAVA_HOME/bin:/zap/:$PATH

ENTRYPOINT ["/develop/gradlew"]
CMD ["integrationTest"]
