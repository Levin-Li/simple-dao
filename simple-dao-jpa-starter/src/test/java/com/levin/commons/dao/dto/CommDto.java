package com.levin.commons.dao.dto;

import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.service.domain.Desc;
import lombok.Data;


import java.util.Date;

@Data
public class CommDto {

    @Desc("是否允许")
    @Ignore
    protected Boolean cache = true;

    @Desc("是否允许")
    protected Boolean enable = true;

    @Desc("是否可编辑")
    protected Boolean editable = true;

    @Desc("创建时间")
    protected Date createTime;

    @Desc("更新时间")
    protected Date lastUpdateTime;

    @Desc("备注")
    protected String remark;

}
