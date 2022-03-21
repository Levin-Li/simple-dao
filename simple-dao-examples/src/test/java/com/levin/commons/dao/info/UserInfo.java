package com.levin.commons.dao.info;

import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.List;


@Data
@Accessors(chain = true)
@ToString(exclude = "group")
@FieldNameConstants
public class UserInfo {

    Long id;

    String state;


    String area;

    String job;

    @Desc("分数")
    Integer score;

    String description;

    @Schema(description = "归属的虚拟组织")
    @InjectVar(converter = PrimitiveArrayJsonConverter.class)
    List<String> belongOrgList;

}
