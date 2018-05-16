#!/bin/bash

export ZAP_PATH /zap/zap.sh
export HOME /home/zap/

/zap/zap.sh -daemon -dir /home/zap/ -port 8090 -host 0.0.0.0 -config api.disablekey=true -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -addoninstall soap -addoninstall openapi &

sleep 5

java -Djava.security.egd=file:/dev/./urandom -Xmx512m -jar /app.jar
