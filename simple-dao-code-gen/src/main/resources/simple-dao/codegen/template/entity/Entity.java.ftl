package ${CLASS_PACKAGE_NAME};

import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.domain.support.AbstractBaseEntityObject;
import com.levin.commons.dao.domain.support.AbstractNamedMultiTenantObject;
import com.levin.commons.dao.domain.support.E_AbstractNamedMultiTenantObject;
import com.levin.commons.dao.domain.*;
import com.levin.commons.service.domain.EnumDesc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.*;
import java.math.*;


@Entity(name = EntityConst.PREFIX + "${entityName}")
@Data
<#if entityPkName??>@EqualsAndHashCode(of = {"${entityPkName}"})</#if>
@Accessors(chain = true)
@FieldNameConstants
@Schema(description = "${entityComment}")

@Table(
<#if entitySchema??>        //schema = "${entitySchema}",</#if>
        indexes = {
               // 索引
               // @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
               // @Index(columnList = AbstractBaseEntityObject.Fields.enable),
               // @Index(columnList = AbstractBaseEntityObject.Fields.createTime),
               // @Index(columnList = AbstractNamedMultiTenantObject.Fields.tenantId),
               // @Index(columnList = E_AbstractNamedMultiTenantObject.name),
               // @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
        },

        uniqueConstraints = {
              //   唯一约束
              //   @UniqueConstraint(columnNames = {AbstractNamedMultiTenantObject.Fields.tenantId, E_${entityName}.code}),
              //   @UniqueConstraint(columnNames = {AbstractNamedMultiTenantObject.Fields.tenantId, E_AbstractNamedMultiTenantObject.name}),
        }
)
public class ${entityName}
 //       extends AbstractBaseEntityObject
 //       extends AbstractNamedMultiTenantObject
          implements  EntityObject
{

private static final long serialVersionUID = ${serialVersionUID}L;

<#list fields as field>
   <#if field.isPk>
    @Id
    @GeneratedValue<#if !field.isIdentity>(generator = "default_uuid")</#if>
   </#if>
   <#if field.isLob>
    @Lob
   </#if>
   <#if field.fieldTypeBox == 'Date'>
    @Temporal(TemporalType.<#if field.columnType =='date'>DATE<#elseif field.columnType =='time'>TIME<#else>TIMESTAMP</#if>)
   </#if>
    @Column(<#if !field.isNullable>nullable = false,</#if><#if field.maxLength?? && field.maxLength &gt; 0 > length = ${field.maxLength?string}</#if><#if field.scale?? && field.scale &gt; 0 >, scale = ${"" + field.scale}</#if>) // db: ${field.columnName} ${field.columnType}
    @Schema(description = "${field.label}")
    protected ${field.fieldTypeBox} ${field.javaFieldName};

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

