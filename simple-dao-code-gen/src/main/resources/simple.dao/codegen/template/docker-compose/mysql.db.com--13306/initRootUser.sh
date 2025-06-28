#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

#等待3秒, 等待mysql启动完成
sleep 3

if [ ! -e "./mysql/data/mysql.ibd" ]; then

#继续等待4秒, 等待mysql启动完成
sleep 4

fi

#mysql是否已经创建
if [ ! -e "./mysql/data/mysql.ibd" ]; then

  echo "数据库未创建, 无法初始化数据库root用户"
  exit 1

fi

#参数1 文件名 参数2 变量名
findLastValue(){
  #读取文件行,找出有等号的行, awk 去除首尾空字符, 获取关键字开始的行, 获取等号右边,删除注释,最后再去除首尾空字符
  cat "${1}" | grep '=' | awk '{$1=$1};1' | grep "^${2}" | awk -F'=' '{print $2}'| awk -F'#' '{print $1}' | tail -n 1 | awk '{$1=$1};1'
}

rootPwd=$(findLastValue ".env" 'MYSQL_ROOT_PASSWORD');
dbPort=$(findLastValue  ".env" 'SERVICE_PORT');

if [ -z "${rootPwd}" ]; then
  echo "数据库非首次启动, 如果需要再次初始化数据库root用户, 可以在配置文件[.env]中设置[MYSQL_ROOT_PASSWORD]为root密码, 并再次运行本脚本[${0}]"
  exit 1

fi

echo "初始化数据库root用户, 允许root用户从任何主机登录并授权所有权限..."

#-p"${rootPwd}"
docker exec -it mysql-${dbPort} mysql -uroot -p"${rootPwd}" \
-e "CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '${rootPwd}'; \
    GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; \
    FLUSH PRIVILEGES;"

execStatus=$? # 获取返回值

#如果存在root密码, 则设置root用户权限
if [ -n "${rootPwd}" ]  && [ $execStatus -eq 0 ]; then

echo "数据库root用户初始化完成, 请牢记数据库root密码:${rootPwd}"

#开始清除root密码
# 将 "old_text" 替换为 "new_text"，输出到新文件

# 如果是 mac os
if [ "$(uname)" == "Darwin" ]; then
  sed -i '' "s/${rootPwd}//g" .env
else
  sed -i "s/${rootPwd}//g" .env
fi

execStatus=$? # 获取返回值

#如果存在root密码, 则设置root用户权限
  if [ $execStatus -eq 0 ]; then
    echo "配置文件[.env]中数据库root密码已清除"
  fi

else

  echo "初始化数据库root用户失败"

fi