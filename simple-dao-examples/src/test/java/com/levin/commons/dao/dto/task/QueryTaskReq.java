package com.levin.commons.dao.dto.task;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.IsNotNull;
import com.levin.commons.dao.domain.Task;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;


@Data
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = Task.class, resultClass = TaskInfo.class)
public class QueryTaskReq {

    @IsNotNull
    String area;

}
