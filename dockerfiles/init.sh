#!/bin/bash

if [ `id -u` -ge 1000 ]; then
    cat /etc/passwd | sed -e "s/^osUser:/builder:/" > /tmp/passwd
    echo "osUser:x:`id -u`:`id -g`:,,,:/home/zap:/bin/bash" >> /tmp/passwd
    cat /tmp/passwd > /etc/passwd
    rm /tmp/passwd
fi

/zap/zap.sh -Xmx3G -daemon -dir /home/zap/ -port 8090 -host 0.0.0.0 -config api.disablekey=true -config api.addrs.addr.name=.* -config api.addrs.addr.regex=true -addoninstall soap -addoninstall openapi -addoninstall ascanrulesBeta -addoninstall ascanrulesAlpha -addoninstall pscanrulesBeta -addoninstall pscanrulesAlpha &

sleep 10

java -Djava.security.egd=file:/dev/./urandom -Xmx2G -jar /home/zap/app.jar
