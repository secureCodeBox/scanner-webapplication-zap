FROM gradle:alpine as builder
USER root
COPY . .
RUN gradle clean build

FROM owasp/zap2docker-stable

USER root

RUN apt-get update && apt-get upgrade -y && apt-get install -y openjdk-8-jdk supervisor python-pip && apt-get clean
RUN pip install supervisor-stdout

COPY dockerfiles/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY dockerfiles/csrfAuthScript.js /zap/scripts/templates/authentication/csrfAuthScript.js
COPY --from=builder /home/gradle/build/libs/scanner-webapplication-zap-0.4.0-SNAPSHOT.jar /app.jar

RUN chown zap:zap /app.jar && \
    chown zap:zap /usr/bin/supervisord && \
    chown zap:zap /var/log/supervisor && \
    chown zap:zap /zap/supervisord.sock && \
    chmod -R g+r /var/log/supervisor && \
    chmod -R g+w /var/log/supervisor && \
    chmod -R 777 /var/log/supervisor

EXPOSE 8080 8090

ENV JAVA_OPTS ""

USER zap

ENTRYPOINT ["/usr/bin/supervisord"]
CMD []
