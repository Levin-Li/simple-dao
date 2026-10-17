# 设计

复用 service-support 的 RbacDataAttrGroupMetadata，实体上的字段、getter、类、继承和重复标注先统一解析；模板按源属性输出显式名称、等级、备注的字段注解，查询派生属性继承同一源组。公共基类承接字段输出类型级 attrList；SimpleQuery 无业务属性时不伪造属性，保留 TargetOption 供 API 校验选择列和排序。
