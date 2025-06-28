#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

source ./initEnv.sh

docker-compose down
