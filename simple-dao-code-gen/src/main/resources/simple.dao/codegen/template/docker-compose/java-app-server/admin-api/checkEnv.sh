#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

# 创建目录
mkdir biz-libs third-libs config resources logs 2>/dev/null

#创建软链接
#ln -fs ../third-libs third-libs 2>/dev/null

# 查找jar包数量
jarCnt=$(find . -maxdepth 1 -name "*.jar" | wc -l | awk '{$1=$1};1')

if [ "${jarCnt}" != "1" ]; then
   echo "***ERROR*** [`pwd`] Spring Boot App jar file must be only one in current directory."
   exit 1
fi

appJars=`ls *.jar`

#添加环境变量
addEnv "APP_JAR" "${appJars}" "Force"

echo "java -jar ${appJars}"


