package com.levin.commons.dao.dto;


import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.select.SelectColumn;
import com.levin.commons.dao.annotation.update.UpdateColumn;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;


@Data
@Accessors(chain = true)
public class UserSelectDTO extends UserDTO {

    @SelectColumn("new com.levin.commons.dao.dto.UserGroupDTO(u,u.group)")
    @Like("group.name")
    String groupName = "Group";

    @Ignore
    @SelectColumn()
    String name;



    @Ignore
    @SelectColumn("score")
    UserStatDTO selectSubQueryDTO = new UserStatDTO();

    @Ignore
    @SelectColumn(value = "score", subQuery = "select 3000 from xxx.tab t where u.id = t.id")
    Map param = new HashMap();

    //子查询，并使用命名参数，命名参数从Map变量中取
    @NotExists(subQuery = "select name from xxx.tab t where u.id = t.id and t.score > ${:minScore} and t.name = ${:groupName}")
//            int minScore =5;
    Map<String, Object> namedParams = new HashMap<>();


    //子查询，子查询将从subQueryDTO查询对象中生成
    @NotExists
    SubQueryDTO subQueryDTO = new SubQueryDTO();

    @In("name")
    SubQueryDTO nameInDto = new SubQueryDTO();

    //子查询产生
    @Gt("score")
    SubQueryDTO gtSubQueryDTO = new SubQueryDTO();


}
