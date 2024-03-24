package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractTreeObject;
import com.levin.commons.service.domain.Desc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_Group")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class Group
        extends AbstractTreeObject<Long, Group>
        implements StatefulObject {

    @Id
    @GeneratedValue
    private Long id;

    //由子类去定义
    @Schema(description = "父ID")
    @Column(length = 128)
    protected Long parentId;

    @Desc("状态")
    String state;

    @Desc("类别")
    String category;

    @Desc("分数")
    Integer score;


    public Group() {
    }

    public Group(String name, Long parentId) {
        super(parentId, name);
    }

    public Group(Long id, String name) {
        super(null, name);
        this.id = id;
    }

    public Group(String name) {
        super(null, name);
    }


    @Override
    public String toString() {
        return name + "[" + state + "]";
    }
}
