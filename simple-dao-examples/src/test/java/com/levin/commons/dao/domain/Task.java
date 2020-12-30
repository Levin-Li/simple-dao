package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_Task")
@Data
@Accessors(chain = true)
public class Task
        extends AbstractNamedEntityObject<Long>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    String state;

    @Column
    String area;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Desc("任务等分")
    Integer score;


    String description;

}
