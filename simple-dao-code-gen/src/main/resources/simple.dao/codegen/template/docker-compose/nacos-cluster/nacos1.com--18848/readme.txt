
部署步骤:

1 修改nacos节点的目录名称, 如 nacos1.com--18848 , nacos1.com 是主机名称, 18848是端口,中间用--分隔.

2 修改../cluster.env文件中到数据库连接信息

3 创建nacos数据库,创建数据库用户nacos,同时授权,导入mysql的建表语句(./nacos/conf/mysql-schema.sql)

    create schema nacos collate utf8mb4_unicode_ci;

    CREATE USER IF NOT EXISTS 'nacos'@'%' IDENTIFIED BY 'nacos123456';

    GRANT ALL PRIVILEGES ON nacos.* TO 'nacos'@'%';

    FLUSH PRIVILEGES;

4 确保配置文件./nacos/conf/application.properties存在,并且正确映射
        - ./nacos/conf:/home/nacos/conf

5 检查./.env ../cluster.env ../custom-hosts 文件内容是否正确.

  检查数据库是否能正常访问.

6 启动并检查日志


集群模式需要修改 cluster.env
# 运行模式
MODE=cluster