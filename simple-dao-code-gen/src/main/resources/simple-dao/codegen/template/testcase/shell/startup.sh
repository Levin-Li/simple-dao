<#noparse>#!/bin/bash
#Author Lilw @2012
execDir=`pwd`

#sh文件所在目录
shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

appJars=`ls *.war`
isFound=`echo $?`

if [ "$isFound" != "0" ]; then
  appJars=`ls *.jar`
  isFound=`echo $?`
fi

if [ "$isFound" != "0" ]; then
   echo "***ERROR*** spring boot launch file(.war or .jar) not found."
   exit 1
fi

mkdir -p "resources/public"
mkdir -p "config"

#读取进程标识
tempFile=`date +%s`

pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

content=""

if [ -z $pids ]; then

#   read -p "是否需要启动密码?[y/n]" -t 7 needParam

   if [ "${needParam}" = "y" ]; then

#      echo "请输入启动密码:"
      echo "Please input startup password:"
      #主要用于输入密码，但不会在命令行历史记录出现
      head -n 1 <&0 > ${tempFile}

   fi

   #如果有文件
   if [ -f ${tempFile} ]; then
       content=`cat ${tempFile}`
   fi

   extName=`uname`

   if [ "${extName}" = "Linux" ]; then
       extName="so"
   fi

   if [ "${extName}" = "Darwin" ]; then
       extName="dylib"
   fi

   #如果文件有内容
   if [ -n "${content}" ]; then
       content="  -DPrintHookAgentLog=true -agentpath:third-libs/libHookAgent.${extName}=${tempFile} -XX:+DisableAttachMechanism "
   fi

   JAVA_CMD=`which ${JAVA_HOME}/bin/java`

   if [ -z "${JAVA_CMD}" ]; then
       JAVA_CMD=`which java`
       echo "***Warning*** JAVA_HOME env var not found，will be use ${JAVA_CMD}"
   fi

   if [ -z "${JAVA_CMD}" ]; then
       echo "***Error*** java cmd not found"
   fi

   globalAppLibs="${G_BOOT_APP_LIBS}"

   if [ -z "$globalAppLibs" ]; then
       echo "***Info*** you can config shell env G_BOOT_APP_LIBS for app lib dir, the dir must be an absolute dir and split by comma."
       globalAppLibs=""
   else
       #增加逗号
       globalAppLibs=",${globalAppLibs}"
   fi


   START_CMD="${JAVA_CMD} -server -Dwork.dir=${shellDir} ${content} -Dloader.path=config,static,resources,biz-libs,third-libs${globalAppLibs} -jar ${appJars}"

   echo "Startup cmd line：${START_CMD}"

   nohup ${START_CMD}  2>&1 &

   sleep 5s

   #覆盖临时文件
   echo "#INVALID_PWD:#param:$$" > ${tempFile}

   #删除临时文件
   rm -fr ${tempFile}

   ps -ef | grep java | grep "$shellDir"

   #如果是人工交互，顺便查看启动过程
   if [ -n "${needParam}" ]; then
       #查看日志
       tail -f *.out
   fi

else

   echo "[$shellDir/$0] program already started."
   ps -ef | grep java | grep "$shellDir"

fi

</#noparse>