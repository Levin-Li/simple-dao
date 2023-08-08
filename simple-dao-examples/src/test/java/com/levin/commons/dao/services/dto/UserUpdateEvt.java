package com.levin.commons.dao.services.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@TargetOption(entityClass = User.class)
@Data
@Accessors(chain = true)
public class UserUpdateEvt {

    @Eq(require = true)
    String id;

    @Update
    String state;

    //更新为 set  score = score + ?

    @Update(value = "score", paramExpr = "${_name} + ${:_val}",)
    Integer addScore = 2;

}
