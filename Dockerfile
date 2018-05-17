FROM gradle:alpine as builder
USER root
COPY . .
RUN gradle clean build

FROM owasp/zap2docker-bare

COPY dockerfiles/init.sh /home/zap/init.sh
COPY dockerfiles/csrfAuthScript.js /home/zap/scripts/templates/authentication/csrfAuthScript.js
COPY --from=builder /home/gradle/build/libs/scanner-webapplication-zap-0.4.0-SNAPSHOT.jar /home/zap/app.jar

USER root
RUN chmod +x /home/zap/init.sh && \
    chgrp -R 0 /home/zap/ && \
    chmod -R g=u /home/zap/
USER zap

EXPOSE 8080 8090

ENV JAVA_OPTS ""

CMD ["/home/zap/init.sh"]
