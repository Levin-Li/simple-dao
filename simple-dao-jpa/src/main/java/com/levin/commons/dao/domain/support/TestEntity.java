package com.levin.commons.dao.domain.support;

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "jpa_dao_test_entity")
@Data
@Accessors(chain = true)
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getState() {
        return state;
    }
}
