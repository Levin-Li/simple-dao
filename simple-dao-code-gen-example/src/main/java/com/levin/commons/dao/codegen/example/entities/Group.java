package com.levin.commons.dao.codegen.example.entities;

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.AbstractTreeObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "exam_groups")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class Group
        extends AbstractTreeObject<Long, Group>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

//    String area;

    @Desc("状态")
    String state;

    @Desc("类别")
    String category;

    @Desc("分数")
    Integer score;


    public Group(String name, Long parentId) {
        super(name, parentId);
    }

    public Group() {
    }

    public Group(Long id, String name) {
        super(name, null);
        this.id = id;
    }

    public Group(String name) {
        super(name, null);
    }


    @Override
    public String toString() {
        return   name + "[" + state+"]";
    }
}
