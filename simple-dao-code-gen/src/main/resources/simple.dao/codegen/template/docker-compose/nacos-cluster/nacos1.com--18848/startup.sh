#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

source ./initEnv.sh

docker-compose up -d --no-recreate

sleep 5

tail -f ./nacos/logs/nacos*.log
