package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by echo on 2015/11/17.
 */
@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
public class TestEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    String name;


    @Schema(description = "创建时间")
    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date createTime;

    @Schema(description = "更新时间")
    @Column
    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

    //@OrderBy
    @Schema(description = "排序代码")
    protected Integer orderCode;

    @Schema(description = "是否允许")
    @Column(nullable = false)
    protected Boolean enable;

    @Schema(description = "是否可编辑")
    @Column(nullable = false)
    protected Boolean editable;

    @Schema(description = "备注")
    @Column(length = 512)
    protected String remark;


    @Column
    String state;

    @Column
    String area;

    String operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @Column
    String job;

    @Desc("分数")
    Integer score;

    String description;


}
