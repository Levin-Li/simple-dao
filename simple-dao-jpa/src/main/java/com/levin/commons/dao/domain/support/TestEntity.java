package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "jpa_dao_test_entity")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class TestEntity
        extends AbstractTreeObject<Long, TestEntity>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Desc("状态")
    String state;

    @Desc("类别")
    String category;

    @Desc("分数")
    Integer score;

    @Desc("操作")
    Op op;

}
