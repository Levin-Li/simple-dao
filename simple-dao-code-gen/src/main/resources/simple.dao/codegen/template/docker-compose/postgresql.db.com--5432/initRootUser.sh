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
maxRetry=30
retry=0

if [ -z "${dbUser}" ]; then
  dbUser="postgres"
fi

echo "等待PostgreSQL启动完成..."

while true
do
  docker exec -i postgresql-${dbPort} pg_isready -U "${dbUser}" >/dev/null 2>&1

  if [ $? -eq 0 ]; then
    break
  fi

  retry=$((retry + 1))

  if [ $retry -ge $maxRetry ]; then
    echo "PostgreSQL 启动超时，输出容器最近200行日志..."
    docker logs --tail 200 postgresql-${dbPort} 2>&1 || docker compose logs --tail 200 2>&1 || true
    exit 1
  fi

  sleep 2
  echo "等待PostgreSQL启动完成...(${retry}/${maxRetry})"
  echo "========== postgresql-${dbPort} 最近20行容器日志 =========="
  docker logs --tail 20 postgresql-${dbPort} 2>&1 || docker compose logs --tail 20 2>&1 || true
done

echo "PostgreSQL 已就绪，使用[${dbUser}]用户登录数据库成功"

if [ -n "${dbPwd}" ]; then
  if [ "$(uname)" = "Darwin" ]; then
    sed -i '' "s/^POSTGRES_PASSWORD=.*/POSTGRES_PASSWORD=/g" .env
  else
    sed -i "s/^POSTGRES_PASSWORD=.*/POSTGRES_PASSWORD=/g" .env
  fi

  execStatus=$?

  if [ $execStatus -eq 0 ]; then
    echo "数据库root用户初始化完成, 请牢记数据库[${dbUser}]用户密码:${dbPwd}"
    echo "配置文件[.env]中的POSTGRES_PASSWORD已清除"
  fi
fi
