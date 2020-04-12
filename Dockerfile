FROM gradle:6.3.0-jdk8 as builder
USER root
COPY . .

ARG COMMIT_ID=unkown
ARG BRANCH=unkown
ARG REPOSITORY_URL=unkown
ARG BUILD_DATE
ARG VERSION

ENV SCB_COMMIT_ID ${COMMIT_ID}
ENV SCB_BRANCH ${BRANCH}
ENV SCB_REPOSITORY_URL ${REPOSITORY_URL}

RUN gradle clean build -Pvcs_commit=${SCB_COMMIT_ID} -Pvcs_version=${SCB_BRANCH} -Pvcs_url=${SCB_REPOSITORY_URL}

FROM owasp/zap2docker-bare:2.9.0

COPY dockerfiles/init.sh /home/zap/init.sh
COPY dockerfiles/csrfAuthScript.js /home/zap/scripts/templates/authentication/csrfAuthScript.js
COPY --from=builder /home/gradle/build/libs/scanner-webapplication-zap-0.4.0-SNAPSHOT.jar /home/zap/app.jar

USER root

RUN wget -O /home/zap/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/54d1f0bfeb6557adf8a3204455389d0901652242/wait-for-it.sh

RUN chmod +x /home/zap/init.sh && \
    chmod +x /home/zap/wait-for-it.sh && \
    chgrp -R 0 /home/zap/ && \
    chmod -R g=u /home/zap/ && \
    chown -R zap /home/zap

USER zap

LABEL org.opencontainers.image.title="secureCodeBox scanner-webapplication-zap" \
    org.opencontainers.image.description="OWASP Zap integration for secureCodeBox" \
    org.opencontainers.image.authors="iteratec GmbH" \
    org.opencontainers.image.vendor="iteratec GmbH" \
    org.opencontainers.image.documentation="https://github.com/secureCodeBox/secureCodeBox" \
    org.opencontainers.image.licenses="Apache-2.0" \
    org.opencontainers.image.version=$VERSION \
    org.opencontainers.image.url=$REPOSITORY_URL \
    org.opencontainers.image.source=$REPOSITORY_URL \
    org.opencontainers.image.revision=$COMMIT_ID \
    org.opencontainers.image.created=$BUILD_DATE

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=3 CMD curl --fail http://localhost:8080/internal/health || exit 1

EXPOSE 8080 8090

ENV JAVA_OPTS ""

CMD ["/home/zap/init.sh"]
