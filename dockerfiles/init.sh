#!/bin/bash
/zap/zap.sh -Xmx3G -daemon -dir /home/zap/ -port 8090 -host 0.0.0.0 -config api.disablekey=true -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -addoninstall soap -addoninstall openapi -addoninstall ascanrulesBeta -addoninstall ascanrulesAlpha -addoninstall pscanrulesBeta -addoninstall pscanrulesAlpha &

/home/zap/wait-for-it.sh --timeout=120 localhost:8090 -- java -Djava.security.egd=file:/dev/./urandom -Xmx2G -jar /home/zap/app.jar
