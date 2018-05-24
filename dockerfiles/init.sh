#!/bin/bash

if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "${USER_NAME:-default}:x:$(id -u):0:${USER_NAME:-default} user:${HOME}:/sbin/nologin" >> /etc/passwd
  fi
fi

/zap/zap.sh -daemon -dir /home/zap/ -port 8090 -host 0.0.0.0 -config api.disablekey=true -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -addoninstall soap -addoninstall openapi &

sleep 5

java -Djava.security.egd=file:/dev/./urandom -Xmx512m -jar /home/zap/app.jar
