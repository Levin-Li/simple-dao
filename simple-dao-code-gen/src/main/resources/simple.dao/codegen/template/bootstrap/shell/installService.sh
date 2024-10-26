<#noparse>#!/bin/bash
#Author Lilw @2024
### 代码生成哈希校验码：[], 请不要修改和删除此行内容。

execDir=`pwd`

#sh文件所在目录
shellDir=`dirname $0`

cd $shellDir

shellDir=`pwd`

#替换掉/为_
service_name=$(echo $shellDir | sed 's/\//_/g')

daemon_service_file="/etc/systemd/system/${service_name}.service"

echo "Gen service[${service_name}]  --> file: ${daemon_service_file}"

#包括双引号
echo "[Unit]" > "${daemon_service_file}"
echo "Description=${service_name} daemon service" >> "${daemon_service_file}"
echo "After=network.target" >> "${daemon_service_file}"
echo "After=network-online.target" >> "${daemon_service_file}"
echo "Wants=network-online.target" >> "${daemon_service_file}"

echo "[Service]" >> "${daemon_service_file}"
echo "ExecStart=${shellDir}/restart.sh" >> "${daemon_service_file}"
echo "ExecStop=${shellDir}/shutdown.sh" >> "${daemon_service_file}"
echo "Type=simple" >> "${daemon_service_file}"
echo "User=${USER}" >> "${daemon_service_file}"
echo "#Group=${USER}" >> "${daemon_service_file}"

echo "[Install]" >> "${daemon_service_file}"
echo "WantedBy=multi-user.target" >> "${daemon_service_file}"

echo "Cat service[${service_name}] file content:"
cat "${daemon_service_file}"

chmod +x "${daemon_service_file}"
chmod +x *.sh

echo "Make service auto start on os boot"

systemctl enable "${service_name}.service"

systemctl status "${service_name}.service"

</#noparse>
