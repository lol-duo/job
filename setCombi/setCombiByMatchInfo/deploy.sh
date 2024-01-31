#!/bin/bash

REPOSITORY=/home/ec2-user/job4-1
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
chmod +x $JAR_NAME
nohup /opt/jdk-17/bin/java -jar -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9905 -Dcom.sun.management.jmxremote.rmi.port=9905 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=43.203.53.58 $JAR_NAME > $REPOSITORY/nohup.out  2>&1 &