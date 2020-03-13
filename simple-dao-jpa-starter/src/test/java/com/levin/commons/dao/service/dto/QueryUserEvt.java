package com.levin.commons.dao.service.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@TargetOption(entityClass = User.class,alias = "u")
@Data
@Accessors(chain = true)
public class QueryUserEvt {

    String id;

    String state;

    String area;

}
