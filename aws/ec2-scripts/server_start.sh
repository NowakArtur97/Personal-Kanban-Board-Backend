#!/usr/bin/env bash
cd /home/ec2-user/server

java -jar -Dserver.port=8080 /home/ec2-user/server/personalkanbanboardbackend-0.0.1-SNAPSHOT.jar > /dev/null 2> /dev/null < /dev/null &