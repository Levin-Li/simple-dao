<#noparse>#!/bin/bash
#Author Lilw @2012
#代码生成哈希校验码：[]

execDir=`pwd`

 #sh文件所在目录
shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

echo "work dir : $shellDir"

chmod +x *.sh

./shutdown.sh
./startup.sh
tail -f *.out

</#noparse>
###
