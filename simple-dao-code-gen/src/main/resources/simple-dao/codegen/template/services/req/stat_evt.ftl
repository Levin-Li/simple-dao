package ${packageName};

<#--import static ${modulePackageName}.ModuleOption.*;-->

<#--import com.oak.api.model.ApiBaseReq;-->
import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.service.domain.*;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import java.util.*;

import ${entityClassName};
import ${entityClassPackage}.*;

////////////////////////////////////
//自动导入列表
<#list importList as imp>
import ${imp};
</#list>
////////////////////////////////////

/**
 *  统计${desc}
 *  //Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "统计${desc}")
@Data

<#if pkField?exists>
${(fields?size > 0) ? string('','//')}@AllArgsConstructor
</#if>

@NoArgsConstructor
@Builder
//@EqualsAndHashCode(callSuper = true)
@ToString
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(nativeQL = false, entityClass = ${entityName}.class, alias = E_${entityName}.ALIAS)
public class ${className} implements ServiceReq {

    private static final long serialVersionUID = ${serialVersionUID}L;

    @Schema(description = "统计${desc}结果")
    @Data
    public static class StatInfo  implements Serializable  {

        //@Schema(description = "累加")
        //@Sum
        //Double amount;

        @Schema(description = "条数大于0")
        @Count(havingOp = Op.Gt)
        Integer cnt = 0;
    }

    @Schema(description = "名称模糊匹配")
    @Contains
    String name;

    @Between(paramDelimiter = "-", patterns = {"yyyyMMdd","yyyyMMdd hh24:mm:ss"})  // 参数将用 - 号分隔，自动转换成 Date 类型
    String betweenCreateTime = "20190101-20220201"; //生成语句 createTime between ? AND ?


}
