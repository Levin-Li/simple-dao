package com.levin.commons.service;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.update.UpdateColumn;
import com.levin.commons.dao.domain.*;
import com.levin.commons.dao.simple.annotation.*;

import java.util.List;

@EntityRepository("用户DAO")
@TargetOption(entityClass = User.class, alias = "u" )
//@QueryRequest
public interface UserDao {


    List<User> find(@Eq Long id, @Like String name,
                    @Gt Integer score, Paging paging);

    @QueryRequest(joinFetchSetAttrs = {"group"})
    User findOne(@Eq Long id, @Like String name,
                 @Eq String category, Paging paging);

    @UpdateRequest
    int update(@Eq Long id, @UpdateColumn String name);

    @DeleteRequest
    int delete(@OR @Eq Long id, String name);

}
