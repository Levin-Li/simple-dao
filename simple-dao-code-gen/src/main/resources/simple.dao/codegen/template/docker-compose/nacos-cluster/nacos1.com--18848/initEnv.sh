#!/bin/sh

# cd to script dir
cd `dirname $0`

# 函数案例
# 定义函数：打印所有参数信息
print_args() {
    echo "参数数量: $#"
    echo "所有参数: $@"
    echo "参数列表: $*"

    echo "逐个参数遍历:"
    for arg in "$@"; do
        echo "  - 参数: $arg"
    done

    echo "参数数组遍历:"
    args=("$@")
    for i in "${!args[@]}"; do
        echo "  - 索引 $i: ${args[$i]}"
    done
}

# 调用函数并传递多个参数
# print_args "文件1.txt" "目录2" "参数3" "带空格的参数 4"

getFirstLocalIp(){
  ifconfig | awk '{$1=$1};1' | grep 'inet '| grep ' netmask ' | grep -v ' 127.0.0.1 ' | awk '{print $2}' | head -1 | awk '{$1=$1};1'
}

#参数1 文件名 参数2 变量名
findLastValue(){
  #读取文件行,找出有等号的行, awk 去除首尾空字符, 获取关键字开始的行, 获取等号右边,删除注释,最后再去除首尾空字符
  cat "${1}" | grep '=' | awk '{$1=$1};1' | grep "^${2}" | awk -F'=' '{print $2}'| awk -F'#' '{print $1}' | tail -n 1 | awk '{$1=$1};1'
}

#参数1 文件名 参数2 关键字 参数3 IP
findHostMapRecord(){
  #读取文件行, awk 去除首尾空字符, 去除注释行, 获取#号左边,最后再去除首尾空字符
  #echo $(cat "${1}" | awk '{$1=$1};1' | grep -v '^#'  | awk -F'#' '{print $1}' | awk '{$1=$1};1' | grep "^${3} " | grep " ${2}" )

  #不匹配IP,找出所有的记录,
  # PCRE（Perl 兼容正则表达式，使用 -p 选项）
  # BRE（基本正则表达式）
  # ERE（扩展正则表达式，使用 -E 选项）
  cat "${1}" | awk '{$1=$1};1' | grep -v '^#'  | awk -F'#' '{print $1}' | awk '{$1=$1};1' | grep "\s${2}\(\s\|\$\)"
}

#参数1 文件名 参数2 关键字 参数3 IP
findHostMapRecordCnt(){
  #读取文件行, awk 去除首尾空字符, 去除注释行, 获取#号左边,最后再去除首尾空字符

  #不匹配IP,找出所有的记录,
  # PCRE（Perl 兼容正则表达式，使用 -p 选项）
  # BRE（基本正则表达式）
  # ERE（扩展正则表达式，使用 -E 选项）

  #
  #域名匹配后必须是空格或行尾
  cat "${1}" | awk '{$1=$1};1' | grep -v '^#'  | awk -F'#' '{print $1}' | awk '{$1=$1};1' | grep -c "\s${2}\(\s\|\$\)"
}

#查找数据库主机映射记录, 参数1 主机名,参数2 IP地址,参数3 文件名
addHostMap(){

  hostTitle="${1}"

  host="${2}"
  ip="${3}"
  hostMapFile="${4}"

  if [ -z "${host}" ]; then
     echo "添加[${hostTitle}]主机映射: 必须指定主机名"
     return
  fi

  if [ -z "${ip}" ]; then
     ip="$(getFirstLocalIp)"
     echo "添加[${hostTitle}]主机映射(${host}): IP地址未指定, 将默认使用本机IP:[${ip}]"
  fi

  if [ -z "${hostMapFile}" ]; then

    #优先检测上级目录
    hostMapFile="../custom-hosts"

    # 上级目录不存在主机映射文件
     if [ ! -e "${hostMapFile}" ]; then
        hostMapFile="./custom-hosts"
     fi

     echo "添加[${hostTitle}]主机映射(${host}): 主机映射文件未指定, 默认使用主机映射文件[${hostMapFile}]"
  fi

  # 如果主机映射文件不存在, 则创建
  if [ ! -e "${hostMapFile}" ]; then
      echo "#自动生成的主机映射记录文件, 生成时间: $(date)" > "${hostMapFile}"
  fi

  #从主机映射文件中查找数据库主机映射记录
  hostMapRecord=$(findHostMapRecord "${hostMapFile}" "${host}" "${ip}")

  #要求主机的对应的IP地址
  hostMapRecordCnt=$(findHostMapRecordCnt "${hostMapFile}" "${host}" "${ip}")

  #echo "主机[${host}]映射记录: ${hostMapRecordCnt} [${hostMapRecord}] "

  #如果没有记录,则添加主机映射记录
  if [ "${hostMapRecordCnt}" = "0" ] ; then
     echo "添加[${hostTitle}]主机映射记录[${ip} ${host}]到配置文件[${hostMapFile}]"

     echo "" >> "${hostMapFile}"
     echo "#自动添加[${hostTitle}]主机(${host} --> ${ip})映射记录, 添加时间: $(date)" >> "${hostMapFile}"
     echo "${ip} ${host}" >> "${hostMapFile}"

  elif [ "${hostMapRecordCnt}" = "1" ]; then #要判读IP是否正确

     if [[ "${hostMapRecord}" != "${ip} "* ]]; then

         confirm=""

         read -p  "***警告*** 当前[${hostTitle}]主机[${host}]映射的IP[${hostMapRecord}]不是预期的IP[${ip}], 请确认是否继续(y/n):" -t 7 confirm

         echo ""

         if [ "${confirm}" != "y" ]; then
             echo "程序主动终止"
             exit 1
         fi

     fi

  elif [ "${hostMapRecordCnt}" -gt "1" ]; then #存在多个记录, 则终止运行
     #一个主机名称映射多个IP时, 在Linux/macOS按hosts 文件中出现的顺序，第一个有效IP被使用.
     #为了避免错误,终止运行
     echo "***错误*** [${hostTitle}]主机映射记录[${host}]存在多条,为了避免错误,不允许存在多条映射记录, 请检查主机映射文件[${hostMapFile}], 并删除不正确的主机映射."
     echo "请检查以下映射记录:"
     echo "${hostMapRecord}"
     exit 1
  fi

}

#自动增加环境记录, 参数1 变量名称, 参数2 变量值, 参数3 覆盖模式, 参数4 文件名
addEnv(){

  varName="${1}"
  varValue="${2}"
  rewriteMode="${3}" #Auto=自动(无值覆盖,有值询问),Skip=有不为空的值则跳过, Ask=询问, Force=强制
  envFile="${4}"

  if [ -z "${varName}" ]; then
     echo "添加环境变量: 必须指定变量名"
     return
  fi

  if [ -z "${rewriteMode}" ]; then
    rewriteMode="Auto"
  fi

  if [ -z "${varValue}" ]; then
     varValue=""
  fi

  if [ -z "${envFile}" ]; then

    #优先检测上级目录
    envFile="../.env"

    # 上级目录不存在变量文件
     if [ ! -e "${envFile}" ]; then
        envFile="./.env"
     fi

     echo "添加环境变量(${varName}): 变量文件未指定, 默认使用变量文件[${envFile}]"
  fi

  # 如果变量文件不存在, 则创建
  if [ ! -e "${envFile}" ]; then
      echo "#自动生成的变量记录文件, 生成时间: $(date)" > "${envFile}"
  fi

  #从变量文件中查找数据库变量记录
  oldEnvValue=$(findLastValue "${envFile}" "${varName}")

  if [ "${rewriteMode}" = "Auto" ]; then #Auto=自动(无值覆盖,有值询问)

     if [ -z "${oldEnvValue}" ]; then #无值覆盖
         rewriteMode="Force"
     else #有值询问
         rewriteMode="Ask"
     fi

  fi

  if [ "${rewriteMode}" = "Ask" ] && [ "${oldEnvValue}" != "${varValue}" ] ; then

         confirm=""

         read -p  "***警告*** 环境变量文件[${envFile}]中存在的变量[${varName}=${oldEnvValue}], 设置的新值为:[${varValue}] , 请确认是否覆盖(y/n):" -t 7 confirm

         echo ""

         if [ "${confirm}" = "y" ]; then
            #旧值设置为空,后续进行覆盖
            rewriteMode="Force"
         fi
  fi

  #没有旧值 或是 覆盖模式为强制
  if [ "${rewriteMode}" = "Force" ] || [ -z "${oldEnvValue}" ] ; then

     echo "添加环境变量记录[${varName}=${varValue}]到配置文件[${envFile}]"

     echo "" >> "${envFile}"
     echo "#自动添加的变量(${varName})记录, 添加时间: $(date)" >> "${envFile}"
     echo "${varName}=${varValue}" >> "${envFile}"

  fi

}

###########################################################
addHostMap "本地主机名" "localhost" "127.0.0.1"
###########################################################

HOST_LOCAL_IP=$(getFirstLocalIp)
echo "本机IP: ${HOST_LOCAL_IP}"

#读取目录
SERVICE_HOSTNAME=$(basename `pwd` | awk -F'--' '{print $1}')
SERVICE_PORT=$(basename `pwd` | awk -F'--' '{print $2}')


#如果有解析出主机名和端口, 则添加主机映射
if [ -n "${SERVICE_HOSTNAME}" ] && [ -n "${SERVICE_PORT}" ]; then

  addHostMap "服务主机" "${SERVICE_HOSTNAME}"

  addEnv "SERVICE_HOSTNAME" "${SERVICE_HOSTNAME}"
  addEnv "SERVICE_PORT" "${SERVICE_PORT}"

fi

##########################################################

mysqlDbHost=$(findLastValue "../cluster.env" "MYSQL_SERVICE_HOST")

addHostMap "nacos数据库主机" "${mysqlDbHost}"
