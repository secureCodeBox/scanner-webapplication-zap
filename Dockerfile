FROM gradle:alpine as builder
USER root
COPY . .
RUN gradle clean build

FROM owasp/zap2docker-bare

COPY dockerfiles/init.sh /init.sh
COPY dockerfiles/csrfAuthScript.js /zap/scripts/templates/authentication/csrfAuthScript.js
COPY --from=builder /home/gradle/build/libs/scanner-webapplication-zap-0.4.0-SNAPSHOT.jar /app.jar

USER root

RUN chown zap:zap /app.jar && \
    chmod +x /init.sh && \
    chown zap:zap /init.sh

EXPOSE 8080 8090

ENV JAVA_OPTS ""

# USER zap

CMD ["/init.sh"]
