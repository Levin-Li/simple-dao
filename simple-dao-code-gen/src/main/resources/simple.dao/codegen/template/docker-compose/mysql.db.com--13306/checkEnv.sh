#!/bin/sh

# cd to script dir
cd `dirname $0`

#mysql是否已经创建
if [ ! -e "./mysql/data/mysql.ibd" ]; then

  rootPwd=$(findLastValue ".env" 'MYSQL_ROOT_PASSWORD')

  #首次启动, 则要求设置数据库root密码
  if [ -z "${rootPwd}" ]; then
     echo "首次启动,请在.env文件中设置数据库root密码, root密码变量名[MYSQL_ROOT_PASSWORD]"
     exit 1
  fi

  #尝试自动设置密码
  #addEnv "MYSQL_ROOT_PASSWORD" "M${RANDOM}R${RANDOM}P${RANDOM}" "Skip"
fi


#如果网络模式network_mode:  host
#修改mysql端口


