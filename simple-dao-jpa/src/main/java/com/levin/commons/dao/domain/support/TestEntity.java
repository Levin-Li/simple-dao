package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.annotation.Op;
import com.levin.commons.dao.domain.OrderableObject;
import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "simple_dao_test_entity")
//@DynamicInsert
//@DynamicUpdate
@Data
@Accessors(chain = true)
@FieldNameConstants
@EntityOption(disableActions = {EntityOption.Action.LogicalDelete}, logicalDeleteFieldName = "state", logicalDeleteValue = "deleted")
public class TestEntity
        extends AbstractTreeObject<TestEntity, TestEntity>
        implements StatefulObject {

    @Id
    @GeneratedValue
    private Long id;

    @Desc("状态")
    @Column(nullable = false)
    String state = "C";

    @Desc("类别")
    String category;

    @Desc("分数")
    Integer score;

    @Desc("操作")
    Op op;

}
