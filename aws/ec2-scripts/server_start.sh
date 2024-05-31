#!/usr/bin/env bash
cd /home/ec2-user/server

nohup java -jar -Dserver.port=8080 /home/ec2-user/server/personalkanbanboardbackend-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &

sleep 10

isExistApp=$(pgrep java)
if [[ -n  $isExistApp ]]; then
  echo "Stopping the application."
  sudo kill -9 $isExistApp
  echo "The application is stopped."
else
  echo "The application is not running."
fi

nohup java -jar -Dserver.port=8080 /home/ec2-user/server/personalkanbanboardbackend-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &