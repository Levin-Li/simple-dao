package com.levin.commons.dao.dto;

import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.stat.GroupBy;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.PostConstruct;
import java.util.Date;

@Data
@Accessors(chain = true)
public class CommDto {

    @Desc("是否允许")
    @Ignore
    protected Boolean cache = true;

    @Desc("是否允许")
    protected Boolean enable = true;

    @Desc("是否可编辑")
    protected Boolean editable = true;

//    @Desc("创建时间")
//    protected Date createTime;

    @Desc("更新时间")
    protected Date lastUpdateTime;

    @Desc("备注")
    protected String remark;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    String createTime;

    @PostConstruct
    void init() {
        System.out.println(getClass().getName() + " init 1 ...");
    }

    @PostConstruct
    void init2() {
        System.out.println(getClass().getName() + " init 2 ...");
    }


    @PostConstruct
    void init3() {
        System.out.println(getClass().getName() + " init 3 ...");
    }

}
