package com.levin.commons.dao.dto.task;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.domain.Task;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import java.util.List;


@Data
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(entityClass = Task.class)
public class CreateTask {

    String name;

    String state;

    String area;

    @Desc("参与者列表，Json List")
    List<Integer> actions;

}
