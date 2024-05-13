#!/usr/bin/env bash
cd /home/ec2-user/server
#sudo java -jar -Dserver.port=8080 -Dspring.profiles.active=local \
#    *.jar > /dev/null 2> /dev/null < /dev/null &

nohup java -jar -Dserver.port=8080 -Dspring.profiles.active=local /home/ec2-user/server/personalkanbanboardbackend-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
