<#noparse>
#!/bin/bash

execDir=`pwd`

shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

mkdir -p "resources/public"

pids=`ps -ef | grep java | grep "$shellDir" | awk '{print $2}'`

if [ -z $pids ]; then

   read -p "is need startup parameter?[y/n]" -t 5 needParam

   param = "`date +%s`"

   if [ "y" = needParam]; then

      echo "请输入启动参数(先按回车再按 CTRL+C结束输入):"
      echo "Please input startup parameter(press enter and ^C over):"

      #主要用于输入密码，但不会在命令行历史记录出现
      cat <&0 >param

   fi

   echo "[$shellDir/$0] program startup ..."

   # -XX:+DisableAttachMechanism 禁止调试
   nohup java -Dwork.dir=${shellDir} -server -Dsp=${param} -XX:+DisableAttachMechanism -Dloader.path=resources,biz-libs,third-libs -jar *.jar 2>&1 &

   sleep 5s

   echo "#param:$$" > param

   rm -fr param

else

   echo "[$shellDir/$0] program already startup."
   ps -ef | grep java | grep "$shellDir"

fi

#回到原目录
#cd $execDir

</#noparse>