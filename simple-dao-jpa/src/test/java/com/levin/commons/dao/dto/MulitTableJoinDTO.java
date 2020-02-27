package com.levin.commons.dao.dto;


import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Gt;
import com.levin.commons.dao.annotation.select.SelectColumn;


@TargetOption(fromStatement = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id", maxResults = 100)
public class MulitTableJoinDTO {


    @SelectColumn("u.id")
    Long uid;

    @SelectColumn("g.id")
    @Gt("g.id")
    Long gid = 2L;

}
