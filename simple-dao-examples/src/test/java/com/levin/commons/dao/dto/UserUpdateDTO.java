package com.levin.commons.dao.dto;



import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.IsNotNull;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;


@TargetOption(entityClass = User.class, alias = "u", maxResults = 1)
public class UserUpdateDTO  {

    @Update
    Integer score = this.hashCode();

    @OrderBy
    @IsNotNull
    String name;

}
