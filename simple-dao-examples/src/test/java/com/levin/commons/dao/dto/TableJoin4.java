package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.C;
import com.levin.commons.dao.annotation.Eq;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.PostConstruct;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Data
@Accessors(chain = true)
@TargetOption(tableName = E_User.CLASS_NAME, alias = E_User.ALIAS,

        resultClass = User.class,

        joinOptions = {
                @JoinOption(tableOrStatement = E_Group.CLASS_NAME, entityClass = Void.class,
                        alias = E_Group.ALIAS, joinColumn = E_Group.F_id,
                        joinTargetAlias = E_User.ALIAS, joinTargetColumn = E_User.F_group)
        })
public class TableJoin4 {

    @Eq
    @Select(value = E_User.ALIAS, domain = C.BLANK_VALUE, alias = C.BLANK_VALUE)
    String name;

    @Ignore
    @PageOption(PageOption.Type.PageIndex)
    int pageIndex = 2;

    @Ignore
    @PageOption(PageOption.Type.PageSize)
    int pageSize = 50;

    @Ignore
    @PageOption(PageOption.Type.RequireTotals)
    boolean requireTotals;

    @PostConstruct
    void init() {
        System.out.println(getClass().getName() + " init 1 ...");
    }

    @PostConstruct
    void init2() {
        System.out.println(getClass().getName() + " init 2 ...");
    }


    @PreUpdate
    void PreUpdate() {
        System.out.println(getClass().getName() + " PreUpdate 3 ...");
    }

    @PreRemove
    void PreRemove() {
        System.out.println(getClass().getName() + " PreRemove 3 ...");
    }

}
