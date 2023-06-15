<#noparse>#!/bin/bash
#Author Lilw @2012
execDir=`pwd`

#sh文件所在目录
shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

#包括双引号
keyword="\"${shellDir}\""

#获取进程ID
pids=`ps -ef | grep java | grep "${keyword}" | awk '{print $2}'`

if [ -z "${pids}" ]; then

   echo "[$shellDir/$0] program already stop, nothing to do."

else
#尝试停止进程
   ps -ef | grep java | grep "${keyword}"

   tempPid=$pids

   for n in {1..30};do

      echo "*** $n *** start kill program $pids ..."

      kill $pids

      sleep 1

      pids=`ps -ef | grep java | grep "${keyword}" | awk '{print $2}'`

      if [ -z "${pids}" ]; then
         echo "program ${tempPid} stopped."
         exit
      fi

   done

fi

##################强制停止进程#######################

pids=`ps -ef | grep java | grep "${keyword}" | awk '{print $2}'`

if [ -n "${pids}" ]; then
   echo "***Warning*** kill -9 $pids ..."
   kill -9 $pids
   sleep 1
fi

</#noparse>
