<#noparse>
#!/bin/bash

execDir=`pwd`

shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

#停止进程

pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

if [ -z $pids ]; then

   echo "[$shellDir/$0] program already stop, nothing to do."

else

   ps -ef | grep java | grep `pwd`

   ps -ef | grep java | grep `pwd` | awk '{print $2}' | xargs kill

   echo "start kill program $pids"

   kill $pids

fi

</#noparse>