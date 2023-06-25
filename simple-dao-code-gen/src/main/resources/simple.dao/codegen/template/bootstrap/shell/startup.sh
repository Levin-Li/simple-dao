<#noparse>#!/bin/bash
#Author Lilw @2012
#代码生成哈希校验码：[]

execDir=`pwd`

#sh文件所在目录
shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

#包括双引号
keyword="\"${shellDir}\""

appJars=`ls *.jar`
isFound=`echo $?`

if [ "${isFound}" != "0" ]; then
  appJars=`ls *.war`
  isFound=`echo $?`
fi

if [ "${isFound}" != "0" ]; then
   echo "***ERROR*** Spring Boot App launch file(.war or .jar) not found."
   exit 1
fi

mkdir -p "resources/public"
mkdir -p "config"

#读取进程标识
tempFile=`date +%s`

pids=`ps -ef | grep java | grep "${keyword}" | awk '{print $2}'`

#加密参数
encryptParams=""

if [ -z "${pids}" ]; then

#   read -p "是否需要启动密码?[y/n]" -t 7 needParam

   if [ "${needParam}" = "y" ]; then

#      echo "请输入启动密码:"
      echo "Please input startup password:"
      #主要用于输入密码，但不会在命令行历史记录出现
      head -n 1 <&0 > ${tempFile}

   fi

   #如果有文件
   if [ -f "${tempFile}" ]; then
       encryptParams=`cat ${tempFile}`
   fi

   extName=`uname`

   if [ "${extName}" = "Linux" ]; then
       extName="so"
   fi

   if [ "${extName}" = "Darwin" ]; then
       extName="dylib"
   fi

   #如果文件有内容
   if [ -n "${encryptParams}" ]; then
       encryptParams=" -DPrintHookAgentLog=true -agentpath:third-libs/libHookAgent.${extName}=${tempFile} -XX:+DisableAttachMechanism "
   fi

   JAVA_CMD=`which ${JAVA_HOME}/bin/java`

   if [ -z "${JAVA_CMD}" ]; then
       JAVA_CMD=`which java`
       echo "***Warning*** JAVA_HOME env var not found，will be use ${JAVA_CMD}"
   fi

   if [ -z "${JAVA_CMD}" ]; then
       echo "***Error*** java cmd not found"
   fi

# 全局的公共库和第3方库
   globalAppCommonLibs="${G_BOOT_APP_COMMON_LIBS}"

   if [ -z "${globalAppCommonLibs}" ]; then
       echo "***Info*** you can config shell env G_BOOT_APP_COMMON_LIBS for app common lib dir, the dir must be an absolute dir and split by comma."
       globalAppCommonLibs=""
   else
       #增加逗号
       globalAppCommonLibs=",${globalAppCommonLibs}"
   fi

   globalAppThirdLibs="${G_BOOT_APP_THIRD_LIBS}"

   #测试本地第三方
   thirdLibs=`ls third-libs/*.jar`
   testThirdLibs=`echo $?`


   if [ -z "${globalAppThirdLibs}" ]; then
       echo "***Info*** you can config shell env G_BOOT_APP_THIRD_LIBS for app third lib dir, the dir must be an absolute dir and split by comma."
       globalAppThirdLibs=""
   elif [ "${testThirdLibs}" = "0" -a "${thirdLibs}" != "" ]; then
        echo "***Info*** use local third libs, ignore global third lib ${globalAppThirdLibs}."
        globalAppThirdLibs=""
   else
       #增加逗号
       globalAppThirdLibs=",${globalAppThirdLibs}"
       echo "***Info*** use global third libs."
   fi

   #测试本应用的第3方库，是否存在

   START_CMD="${JAVA_CMD} -server -Dwork.dir=\"${shellDir}\" ${encryptParams} -Dloader.path=config,resources,biz-libs,common-libs${globalAppCommonLibs},third-libs${globalAppThirdLibs} -jar ${appJars}"

   echo "Startup cmd line：${START_CMD}"

   nohup ${START_CMD}  2>&1 &

   sleep 5

   #覆盖临时文件
   echo "#INVALID_PWD:#param:$$" > ${tempFile}

   #删除临时文件
   rm -fr "${tempFile}"

   pList=`ps -ef | grep java | grep "${keyword}"`

#  如果应用没有启动成功
   if [ -z "${pList}" ]; then
     echo "***ERROR*** Spring Boot App [${appJars}] startup fail."
     tail -n 20 nohup.out
     exit 1
   fi

   echo "${pList}"

   #如果是人工交互，顺便查看启动过程
   if [ -n "${needParam}" ]; then
       #查看日志
       tail -f nohup.out
   else
       tail -n 20 nohup.out
   fi

else

   echo "[$shellDir/$0] program already started."
   ps -ef | grep java | grep "${keyword}"

fi


</#noparse>
###
