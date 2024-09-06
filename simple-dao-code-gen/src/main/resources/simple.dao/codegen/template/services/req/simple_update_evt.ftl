package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->
import static ${modulePackageName}.entities.EntityConst.*;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import io.swagger.v3.oas.annotations.media.Schema;
import com.levin.commons.dao.annotation.Ignore;

import com.levin.commons.service.domain.*;
import com.levin.commons.service.support.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.validation.constraints.*;
import javax.annotation.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

import ${entityClassName};
import ${entityClassPackage}.*;
import static ${entityClassPackage}.E_${entityName}.*;
import ${modulePackageName}.services.commons.req.*;

////////////////////////////////////
//自动导入列表
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

/**
 * 更新${entityTitle}
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Schema(title = UPDATE_ACTION + BIZ_NAME)
@Data
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
@NoArgsConstructor
//@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)

//字段更新策略，强制更新时，只要字段被调用set方法，则会被更新，不管是否空值。否则只有值不为[null，空字符串, 空数组，空集合]时才会被更新。
@Update(condition = "isForceUpdateField(#_fieldName) || #" + C.VALUE_NOT_EMPTY)
public class ${className} extends ${reqExtendClass} {

    private static final long serialVersionUID = ${serialVersionUID}L;

    ///////////////////////////强制更新的字段列表///////////////////////////////////////

    @Ignore //dao 忽略
    @Schema(title = "强制更新的字段列表", description = "在该字段列表中的字段值会被强制更新数据库，不管字段值是否为空")
    //@JsonIgnore
    protected LinkedHashSet<String> forceUpdateFields = new LinkedHashSet<>(5);

    @Schema(title = "自动增加强制更新字段", description = "自动增加强制更新字段为true时，只要字段的set方法被调用(对应客户端来说只要上传了参数)，字段会被加入强制更新列表。")
    @Ignore //dao 忽略
    //@JsonIgnore
    protected boolean autoAddForceUpdateField;

    //////////////////////////////////////////////////////////////////

<#if classModel.isType('com.levin.commons.dao.domain.EditableObject')>
    @Schema(description = "可编辑条件，如果是web环境需要增加可编辑的过滤条件" , hidden = true)
    @Eq(condition = IS_WEB_CONTEXT + " && !#_isQuery && " + NOT_SUPER_ADMIN)
    @JsonIgnore
    final boolean eqEditable = true;

</#if>
<#-- 字段分组，参考 CRUD枚举，UPDATE_fields 表示更新分组 -->
<#list UPDATE_fields as field>
<#--    <#if !field.notUpdate && (!field.lazy || field.baseType) && field.baseType && !field.jpaEntity >-->
    <#if !field.notUpdate && !field.jpaEntity >
    <#list field.annotations as annotation>
<#--        <#if !(annotation?string)?contains("@NotNull")>-->
    ${annotation}
<#--        </#if>-->
    </#list>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if> ${field.hidden?string(' , hidden = true', '')})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};

    </#if>
</#list>
<#-- 字段分组，参考 CRUD枚举，默认是 CRUD.DEFAULT 分组，没有前缀 -->
<#list fields as field>
<#--    <#if !field.notUpdate && (!field.lazy || field.baseType) && field.baseType && !field.jpaEntity >-->
    <#if !field.notUpdate && !field.jpaEntity >
    <#list field.annotations as annotation>
<#--    <#if !(annotation?string)?contains("@NotNull")>-->
    ${annotation}
<#--    </#if>-->
    </#list>
    <#if field.optimisticLock>
    @Eq(desc = "乐观锁更新条件")
    @Update(incrementMode = true, paramExpr = "1", condition = "", desc = "乐观锁版本号 + 1")
    </#if>
    @Schema(title = ${field.schemaTitle}<#if field.desc != ''> , description = ${field.schemaDesc}</#if>${field.hidden?string(' , hidden = true', '')})
    ${(field.modifiersPrefix!?trim!?length > 0)?string(field.modifiersPrefix, '')}${field.typeName} ${field.name};

    </#if>
</#list>

    /**
    * 构造函数
    * @param autoAddForceUpdateField
    */
    public ${className}(boolean autoAddForceUpdateField) {
        this.autoAddForceUpdateField = autoAddForceUpdateField;
    }

    @PostConstruct
    public void preUpdate() {
        //@todo 更新之前初始化数据
<#list fields as field>
    <#if classModel.isDefaultUpdateTime(field.name)>

        if(get${field.name?cap_first}() == null){
            set${field.name?cap_first}(<#if field.typeName =='Date'>new ${field.typeName}()<#else>${field.typeName}.now()</#if>);
        }
    </#if>
</#list>
    }

<#list fields as field>
<#--    <#if !field.notUpdate && (!field.lazy || field.baseType) && field.baseType && !field.jpaEntity >-->
    <#if !field.notUpdate && !field.jpaEntity >
    public <T extends ${className}> T set${field.name?cap_first}(${field.typeName} ${field.name}) {
        this.${field.name} = ${field.name};
        return autoAddForceUpdateField(E_${entityName}.${field.name});
    }
   </#if>
</#list>

   ////////////////////////////////////////////////////////////////////////////////

     /**
     * 设置强制更新字段
     * <p>
     * 将会清除原有的字段列表
     *
     * @param forceUpdateFields
     * @return
     */
     public <T extends ${className}> T setForceUpdateFields(Set<String> forceUpdateFields) {

         this.autoAddForceUpdateField = false;

         this.forceUpdateFields.clear();

         if (forceUpdateFields != null) {
         this.forceUpdateFields.addAll(forceUpdateFields);
         }

         return (T) this;
     }

    /**
    * 是否更新字段
    *
    * @param fieldName
    * @return
    */
    public boolean isForceUpdateField(String fieldName) {
        return getForceUpdateFields().contains(fieldName);
    }

    /**
    * 是否更新字段，并删除更新标记，下次调用将不再更新
    *
    * @param fieldName
    * @return 需要更新字段返回 true
    */
    public <T extends ${className}> T removeForceUpdateField(String fieldName) {
        boolean ok = getForceUpdateFields().remove(fieldName);
        return (T) this;
    }

    /**
    * 添加更新字段
    *
    * @param fieldName
    * @return
    */
    public <T extends ${className}> T addForceUpdateField(String fieldName) {
        boolean ok = getForceUpdateFields().add(fieldName);
        return (T) this;
    }

   /**
   * autoAddForceUpdateField 为true 时，添加更新字段
   *
   * @param fieldName
   * @return
   */
   public <T extends ${className}> T autoAddForceUpdateField(String fieldName) {
       boolean ok = autoAddForceUpdateField && getForceUpdateFields().add(fieldName);
       return (T) this;
   }
}
