package com.levin.commons.dao.dto.task;

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
public class TaskInfo {

    @Column
    String state;

    @Column
    String area;

    @Desc("参与者列表，Json List")
    @InjectVar(converter = DefaultJsonConverter.class)
    List<Integer> actions;

}
