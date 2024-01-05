#!/bin/bash

REPOSITORY=/home/ec2-user/job1-2
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
chmod +x $JAR_NAME
nohup /opt/jdk-17/bin/java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &