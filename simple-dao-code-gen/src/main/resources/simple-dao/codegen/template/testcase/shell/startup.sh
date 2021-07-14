<#noparse>
#!/bin/bash

execDir=`pwd`

shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

mkdir -p "resources/public"

pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

if [ -z $pids ]; then

   echo "[$shellDir/$0] program startup ..."
   nohup java -Dwork.dir=${shellDir} -server -Dloader.path=resources,biz-libs,third-libs -jar *.jar 2>&1 &

else

   echo "[$shellDir/$0] program already startup."
   ps -ef | grep java | grep "$shellDir"

fi

#回到原目录
#cd $execDir

</#noparse>