package com.levin.commons.dao.repository;


import com.levin.commons.dao.Paging;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Like;
import com.levin.commons.dao.annotation.logic.OR;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.domain.User;
import com.levin.commons.dao.repository.annotation.DeleteRequest;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.dao.repository.annotation.QueryRequest;
import com.levin.commons.dao.repository.annotation.UpdateRequest;

import java.util.List;

@EntityRepository("用户DAO")
@TargetOption(entityClass = User.class, alias = "u" )
//@QueryRequest
public interface UserDao {


    List<User> find(@Eq Long id, @Contains String name,
                    @Gt Integer score, Paging paging);

    @QueryRequest(joinFetchSetAttrs = {"group"})
    User findOne(@Eq Long id, @Contains String name ,
                 @Eq String category, Paging paging);

    @UpdateRequest
    int update(@Eq Long id, @Update String name);

    @DeleteRequest
    int delete(@OR @Eq Long id, String name);

}
