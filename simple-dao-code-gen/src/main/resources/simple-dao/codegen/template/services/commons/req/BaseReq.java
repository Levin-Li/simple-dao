package ${modulePackageName}.services.commons.req;


import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.service.support.InjectConsts;
import io.swagger.v3.oas.annotations.media.Schema;

import com.levin.commons.dao.*;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.update.*;
import com.levin.commons.dao.annotation.select.*;
import com.levin.commons.dao.annotation.stat.*;
import com.levin.commons.dao.annotation.order.*;
import com.levin.commons.dao.annotation.logic.*;
import com.levin.commons.dao.annotation.misc.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;

import javax.validation.constraints.*;

import lombok.*;
import lombok.experimental.*;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 *  基本查询对象
 *  @Author Auto gen by simple-dao-codegen ${.now}
 */
@Schema(description = "基本查询对象")
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class BaseReq
        implements
//        OrganizedObject ,
        ServiceReq  {

    //注意需要在注入服务中设置isTenantAdmin变量
//    @InjectVar(value = InjectConsts.ORG_ID
//            , isOverride = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_TENANT_ADMIN // 如果不是租户管理员, 那么覆盖必须的
//            , isRequired = InjectVar.SPEL_PREFIX + "!#" + InjectConsts.IS_TENANT_ADMIN // 如果不是租户管理员，那么值是必须的
//    )
//    @Schema(description = "机构ID" , hidden = true)
//    @Eq
//    protected String orgId;

    /**
     * 是否非空
     * @param value
     * @return
     */
    protected boolean isNotBlank(Object value){
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }

}
