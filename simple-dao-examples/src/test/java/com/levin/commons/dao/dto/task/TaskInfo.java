package com.levin.commons.dao.dto.task;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.Task;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.DefaultJsonConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import java.util.List;


@Data
@Accessors(chain = true)
@FieldNameConstants
@Update
@TargetOption(entityClass = Task.class)
public class TaskInfo {

    @Column
    @Eq
    String state;

    @Column
    String area;

    @Desc("参与者列表，Json List")
    @InjectVar(converter = DefaultJsonConverter.class, expectBaseType = CharSequence.class, isRequired = "false")
    List<Integer> actions;

}
