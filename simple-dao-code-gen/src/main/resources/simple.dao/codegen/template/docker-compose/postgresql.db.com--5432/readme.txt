1  请把文件夹名称改为主机加端口的名称

如 xxx.com--5432 , xxx.com 是主机名称, 5432是端口,中间用--分隔.

2 设置./.env文件中的数据库管理员密码
   POSTGRES_PASSWORD=XXXPWD

3 数据库首次启动成功后，脚本会自动清除./.env中的 POSTGRES_PASSWORD
