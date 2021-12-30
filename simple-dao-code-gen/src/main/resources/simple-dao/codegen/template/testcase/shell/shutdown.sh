<#noparse>#!/bin/bash

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

   for n in {1..30};do

      echo "*** $n *** start kill program $pids ..."

      kill $pids

      sleep 1

      pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

      if [ -z $pids ]; then
         exit
      fi

   done

fi

##################强制停止进程#######################

pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

if [ -n "$pids" ]; then
   echo "***Warning*** kill -9 $pids ..."
   kill -9 $pids
   sleep 1
fi

</#noparse>