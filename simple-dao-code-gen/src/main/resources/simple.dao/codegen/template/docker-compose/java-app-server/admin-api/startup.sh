#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

source ./initEnv.sh

source ./checkEnv.sh

docker compose up -d

sleep 7

tail -f ./logs/*.log
