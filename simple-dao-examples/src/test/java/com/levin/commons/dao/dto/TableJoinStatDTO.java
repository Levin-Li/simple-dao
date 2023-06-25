package com.levin.commons.dao.dto;


import com.levin.commons.dao.JoinOption;
import com.levin.commons.dao.TargetOption;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.annotation.order.OrderBy;
import com.levin.commons.dao.annotation.stat.Avg;
import com.levin.commons.dao.annotation.stat.Count;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.dao.annotation.stat.Sum;
import com.levin.commons.dao.domain.E_Group;
import com.levin.commons.dao.domain.E_User;
import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TargetOption(
        nativeQL = true,
        entityClass = User.class, //主表
        alias = E_User.ALIAS, //主表别名
        resultClass = TableJoinStatDTO.class, //结果类
        safeMode = true, //是否安全模式，安全模式时无法执行无条件的查询
        //连接表
        joinOptions = {
                @JoinOption(entityClass = Group.class, alias = E_Group.ALIAS)  //连接的表，和别名
        })
public class TableJoinStatDTO {

    //    统计部门人数
    @Count(havingOp = Op.Gt, orderBy = @OrderBy)
    Integer userCnt = null;

    //    统计部门总得分
    @Sum
    Long sumScore;

    //    统计部门平均分
    @Avg(havingOp = Op.Gt, orderBy = @OrderBy, alias = "avg")
    Long avgScore = 20L;

    //按部门分组统计，结果排序
    @GroupBy(domain = E_Group.ALIAS, value = E_Group.name, orderBy = @OrderBy())
    String groupName;

}
