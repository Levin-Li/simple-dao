<#noparse>#!/bin/bash
#Author Lilw @2024
### 代码生成哈希校验码：[], 请不要修改和删除此行内容。

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

   #查看CPU占用高的线程
   top -Hp ${pids}

fi

</#noparse>
