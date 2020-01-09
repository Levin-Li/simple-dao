package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractTreeObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_Group")
@Data
@Accessors(chain = true)
public class Group
        extends AbstractTreeObject<Long, Group>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Desc("状态")
    String state;

    @Desc("类别")
    String category;

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

//    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }



    @Override
    public String toString() {
        return   super.toString() + " " + state;
    }
}
