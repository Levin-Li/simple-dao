package ${CLASS_PACKAGE_NAME};


import com.levin.commons.dao.*;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.service.domain.*;
import com.levin.commons.dao.domain.support.*;

import com.levin.commons.service.support.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.*;
import java.math.*;
//import org.hibernate.annotations.*;

import static ${CLASS_PACKAGE_NAME}.E${entityName}.*;


/**
*
* @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
*
*/
@Data
<#if entityPkName??>@EqualsAndHashCode(of = {"${entityPkName}"})</#if>
@Accessors(chain = true)
@FieldNameConstants
@ToString(callSuper = true)
@Schema(description = "${entityComment}")
@Entity(name = EntityConst.PREFIX + "${entityName}")
@Table(
<#if entitySchema??>        //schema = "${entitySchema}",</#if>
indexes = {
// 索引
<#if !attrs.test('name')>//</#if>                 @Index(columnList = E_${entityName}.name),

<#list fields as field>
    <#if !field.isPk && keywordFun.test(field.camelCaseName,'id,no,time,date,name,status,state,type,code,category') >
        @Index(columnList =  E_${entityName}.${field.camelCaseName}),
    <#elseif field.isPk && attrs.test('tenantId')>
        @Index(columnList = E_${entityName}.tenantId + "," + E_${entityName}.${field.camelCaseName}),
    </#if>
</#list>
},

uniqueConstraints = {
//   唯一约束
//   @UniqueConstraint(columnNames = {AbstractNamedMultiTenantObject.Fields.tenantId, E_${entityName}.code}),
//   @UniqueConstraint(columnNames = {AbstractNamedMultiTenantObject.Fields.tenantId, E_AbstractNamedMultiTenantObject.name}),
}
)

//JPA 继承配置
//@Inheritance(strategy = InheritanceType.JOINED)//定义实体类的继承策略，这里表示使用Joined Table的继承策略，子类和父类分别映射到不同的数据库表
//@DiscriminatorColumn(name = E_${entityName}.exType)//DiscriminatorColumn注解指定了区分列的名称

//DiscriminatorColumn 字段例子
//@Column(length = 64, nullable = false, insertable = false, updatable = false)
//String exType;

//当前类和子类都要配置鉴别值
//@DiscriminatorValue(E_${entityName}.SIMPLE_CLASS_NAME)

//关于 JPA 继承模型
//@DiscriminatorColumn
//@DiscriminatorValue
//@PrimaryKeyJoinColumn(name="aId", referencedColumnName="id")
//@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
//@MappedSuperclass告诉JPA提供者包含基类的持久性属性，就好像它们是由扩展用@MappedSuperclass注解的超类的子类所声明的@MappedSuperclass 。
//但是，inheritance仅在OOP世界中是可见的，因为从数据库的angular度来看，没有任何基类的迹象。 只有子类实体将有一个关联的映射表。
//@Inheritance注释是为了实现数据库表结构中的OOP inheritance模型。 更多的，你可以查询用@Inheritance注解的基类，但是你不能用@MappedSuperclass注解的基类。
//现在，您要使用@Inheritance JPA注释的原因是要实施像“战略模式”这样的行为驱动模式 。另一方面， @MappedSuperclass只是一种重用基本属性，关联，甚至是使用公共基类的实体@Id方法。
//不过，使用@Embeddabletypes可以达到几乎相同的目标。 唯一的区别是你不能重复@Embeddable的@Id定义，但你可以用@MappedSuperclass 。

//实体模型的类别
@EntityCategory(EntityOpConst.BIZ_TYPE_NAME)
//逻辑删除的例子
//@EntityOption(disableActions = EntityOption.Action.Delete,logicalDeleteFieldName = "deleted",logicalDeleteValue = "true")

//JPA 监听器
//@EntityListener(${entityName}Listener.class)

//JPA二级缓存
//@Cacheable(false) //禁止或启用缓存，默认为true，在实体类对应的服务类${entityName}Service中会启用或是禁用缓存

//对父类的属性定义进行覆盖，通常是对数据库相关的（如字段长度，是否允许空等）属性重新定义
//@AttributeOverride(name = E_${entityName}.tenantId, column = @Column(nullable = false, length = 128))
//@AttributeOverride(name = E_${entityName}.orgId, column = @Column(nullable = false, length = 128))

//关联属性重新定义
//@AssociationOverride(name = E_${entityName}.tenantId)
//@AssociationOverride(name = E_${entityName}.orgId)

public class ${entityName}
//    extends AbstractBaseEntityObject
//    extends AbstractMultiTenantObject
//    extends AbstractNamedMultiTenantObject
implements EntityObject
{

private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>
    <#if field.isPk>
        @Id
        @GeneratedValue<#if !field.isIdentity>(generator = "default_id")</#if>
    </#if>
    <#if field.isLob>
        @Lob
    </#if>
    <#if field.fieldTypeBox == 'Date'>
        @Temporal(TemporalType.<#if field.columnType =='date'>DATE<#elseif field.columnType =='time'>TIME<#else>TIMESTAMP</#if>)
    </#if>
    @Column(<#if !field.isNullable>nullable = false,</#if><#if field.maxLength?? && field.maxLength &gt; 0 > length = ${field.maxLength?string}</#if><#if field.scale?? && field.scale &gt; 0 >, scale = ${"" + field.scale}</#if>) // db: ${field.columnName} ${field.columnType}
    @Schema(title = "${field.title}"<#if field.desc != ''>, description = "${field.desc}"</#if>)
    protected ${field.fieldTypeBox} ${field.camelCaseName};

</#list>
//@Override
@PrePersist
public void prePersist() {
//super.prePersist();
}

//@Override
@PreUpdate
public void preUpdate() {
// super.preUpdate();
}

}

