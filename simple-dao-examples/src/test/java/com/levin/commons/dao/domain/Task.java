package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.DefaultJsonConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_Task")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uuid"})
})
@Data
@Accessors(chain = true)
@FieldNameConstants
public class Task
        extends AbstractNamedEntityObject
        implements StatefulObject {

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

    @Desc("参与者列表，Json List")
    @InjectVar(converter = DefaultJsonConverter.class)
    @Lob
    String actions;

}
