package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.PageOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.Gte;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.select.Select;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.annotation.PostConstruct;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Data
@Accessors(chain = true)
@TargetOption(tableName = E_User.CLASS_NAME, alias = E_User.ALIAS,

        resultClass = TableJoin3.class,

        joinOptions = {
                @JoinOption(tableOrStatement = E_Group.CLASS_NAME, entityClass = Void.class,
                        alias = E_Group.ALIAS, joinColumn = E_Group.F_id,
                        joinTargetAlias = E_User.ALIAS, joinTargetColumn = E_User.F_group)
        })
public class TableJoin3 {

    @Select(domain = E_User.ALIAS, value = E_User.id, distinct = true)
    @Gt(value = E_User.id, domain = E_User.ALIAS)
    Long uid = 1l;

    @Select(value = E_Group.id, domain = E_Group.ALIAS)
    @Gte(domain = E_Group.ALIAS, value = E_Group.F_id)
    Long gid;

    @Select
    String name;

    @Select(domain = E_Group.ALIAS, value = E_Group.F_name)
    String groupName;

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
