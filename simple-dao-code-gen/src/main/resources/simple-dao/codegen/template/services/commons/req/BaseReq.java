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

//    @Schema(description = "组织ID" , hidden = true)
//    @InjectVar(value = InjectConsts.ORG_ID , isRequired = "false")
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
