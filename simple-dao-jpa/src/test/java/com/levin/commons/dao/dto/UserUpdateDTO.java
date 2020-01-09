package com.levin.commons.dao.dto;


import com.levin.commons.dao.annotation.select.SelectColumn;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Sum;
import com.levin.commons.dao.annotation.update.UpdateColumn;

import java.util.HashMap;
import java.util.Map;


public class UserUpdateDTO extends UserDTO {

    @UpdateColumn
    Integer score = this.hashCode();

//    @UpdateColumn("state")
//    UserStatDTO selectSubQueryDTO = new UserStatDTO();

//    @UpdateColumn(value = "score", subQuery = "select 3000 from xxx.tab t where u.id = ${:oak}")
    Map param = new HashMap();

}
