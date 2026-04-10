#!/bin/sh

# cd to script dir
cd `dirname $0`

echo "`pwd` $0..."

#参数1 文件名 参数2 变量名
findLastValue(){
  cat "${1}" | grep '=' | awk '{$1=$1};1' | grep "^${2}" | awk -F'=' '{print $2}' | awk -F'#' '{print $1}' | tail -n 1 | awk '{$1=$1};1'
}

dbPort=$(findLastValue ".env" 'SERVICE_PORT')
dbUser=$(findLastValue ".env" 'POSTGRES_USER')
dbPwd=$(findLastValue ".env" 'POSTGRES_PASSWORD')
rootPwd=$(findLastValue ".env" 'POSTGRES_ROOT_PASSWORD')

if [ -z "${dbUser}" ]; then
  dbUser="postgres"
fi

if [ -z "${dbPwd}" ]; then
  echo "未找到数据库管理员密码[POSTGRES_PASSWORD], 跳过附加用户初始化"
  exit 0
fi

echo "等待PostgreSQL启动完成..."

while true
do
  docker exec -i postgresql-${dbPort} pg_isready -U "${dbUser}" >/dev/null 2>&1

  if [ $? -eq 0 ]; then
    break
  fi

  sleep 2
  echo "等待PostgreSQL启动完成..."
done

if [ -z "${rootPwd}" ]; then
  echo "未设置[POSTGRES_ROOT_PASSWORD], 默认使用[${dbUser}]超级用户登录数据库"
  exit 0
fi

echo "初始化兼容root登录角色..."

docker exec -i -e PGPASSWORD="${dbPwd}" postgresql-${dbPort} \
  psql -v ON_ERROR_STOP=1 -U "${dbUser}" -d postgres \
  -c "DO \$\$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'root') THEN CREATE ROLE root WITH LOGIN SUPERUSER PASSWORD '${rootPwd}'; ELSE ALTER ROLE root WITH LOGIN SUPERUSER PASSWORD '${rootPwd}'; END IF; END \$\$;"

execStatus=$?

if [ $execStatus -eq 0 ]; then

  echo "兼容root登录角色初始化完成, 请牢记root密码:${rootPwd}"

  if [ "$(uname)" = "Darwin" ]; then
    sed -i '' "s/^POSTGRES_ROOT_PASSWORD=.*/POSTGRES_ROOT_PASSWORD=/g" .env
  else
    sed -i "s/^POSTGRES_ROOT_PASSWORD=.*/POSTGRES_ROOT_PASSWORD=/g" .env
  fi

  execStatus=$?

  if [ $execStatus -eq 0 ]; then
    echo "配置文件[.env]中的POSTGRES_ROOT_PASSWORD已清除"
  fi

else

  echo "初始化兼容root登录角色失败"

fi
