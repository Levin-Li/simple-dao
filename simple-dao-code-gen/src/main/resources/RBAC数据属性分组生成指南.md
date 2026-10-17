# RBAC 数据属性分组生成指南

实体仅在需要限制 API 属性时声明 `com.levin.commons.rbac.RbacDataAttrGroup`。未声明有效分组的模型不输出分组信息；该生成能力不单独授予权限，也不在业务服务或 DAO 内执行字段限制。

```java
@RbacDataAttrGroup(name = "财务信息", attrList = {"amount", "taxRate"})
@RbacDataAttrGroup(name = "联系方式", attrList = {"phone"})
class Contract {
    @Schema(title = "合同金额") BigDecimal amount;
    BigDecimal taxRate;
    String phone;
    @RbacDataAttrGroup
    @Schema(title = "结算账号") String account;
}
```

可标注类、字段及 JavaBean getter，可重复标注；同名组取属性并集、最低等级取最高值。默认组名在源实体属性的 `Schema.title` 上解析。生成器通过共享 `RbacDataAttrGroupMetadata` 规范化已编译实体，包括继承和无源码依赖，不依赖源码注解复制白名单。

创建请求、SimpleUpdate/Update、SimpleQuery/Query、Info、通用 IdReq 和删除请求 保持 `TargetOption.entityClass`。实际生成属性输出显式名称、最低等级和备注；`betweenDay`、`gteDay`、`inStatus`、`loadAttachment` 等条件/加载参数承接原属性分组，不从派生字段的新标题重新命名。源实体类级 `attrList` 不会原样复制到不存在同名属性的查询类型。

请求及 Info 公共基类承接的租户、组织、个人属性使用生成类型级的实际属性列表；租户/组织/个人显示名称承接对应 ID 的组。SimpleQuery 不包含业务条件属性，保留 `TargetOption` 供 API 通用处理读取源实体规则检查 `selectColumns`、`orderBy` 等字段引用，不伪造业务属性。

模型中的注解只提供 API 入参和 JSON 出参所需静态元数据。应用应按接口操作合并请求与响应组，采用同一组授权；无权请求整次拒绝、无权 JSON 属性在写键前省略。注解扫描结果及无注解结果均应按编译类型缓存，不能每次序列化重新反射。

验证入口为 `RbacDataAttrGroupCodegenTest`：真实编译无源码实体 JAR，经模型构建与全部模板生成后再次编译，反射检查来源、字段、查询派生及继承分组，验证重复生成稳定。生成后还必须在接入应用中验证真实 MVC、角色授权、显式空值和嵌套输出；本模块测试不替代运行时安全验收。
