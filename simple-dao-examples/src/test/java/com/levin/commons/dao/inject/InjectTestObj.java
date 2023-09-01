package com.levin.commons.dao.inject;


import com.levin.commons.dao.CtxVar;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.Lt;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.DefaultJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
public class InjectTestObj {

    @Data
    @NoArgsConstructor
    @Select
    public static class QResult {

        //@Select
        String name;

        //@Select
        Integer score;

        String state;

    }

    @InjectVar(domain = "dao",  expectBaseType = String.class,  converter = DefaultJsonConverter.class, isRequired = "false")
    List<QResult> product_infos;

}
