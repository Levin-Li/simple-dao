package com.levin.commons.dao.dto;

import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.stat.Count;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.User;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
@TargetOption(entityClass = User.class)
public class UserCaseStatDTO {

    String[] states = {"正常", "已取消", "审请中", "已删除", "已冻结"};


    @Count(E_User.state)
    int state1Cnt;

    @GroupBy
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    String createTime;

}
