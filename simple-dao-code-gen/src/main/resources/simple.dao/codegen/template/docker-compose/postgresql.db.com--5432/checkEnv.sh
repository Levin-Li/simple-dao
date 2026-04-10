#!/bin/sh

# cd to script dir
cd `dirname $0`

# PostgreSQL 是否已经初始化
if [ ! -e "./postgresql/data/PG_VERSION" ]; then

  dbPwd=$(findLastValue ".env" 'POSTGRES_PASSWORD')

  # 首次启动, 则要求设置数据库管理员密码
  if [ -z "${dbPwd}" ]; then
     echo "首次启动,请在.env文件中设置数据库管理员密码, 变量名[POSTGRES_PASSWORD]"
     exit 1
  fi
fi
