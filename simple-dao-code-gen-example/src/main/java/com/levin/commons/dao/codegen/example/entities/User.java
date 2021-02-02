package com.levin.commons.dao.codegen.example.entities;

import com.levin.commons.dao.domain.StatefulObject;
import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */

@Table
@Entity(name = ModuleTableOption.PREFIX + "exam_users")
@Data
@Accessors(chain = true)
@ToString(exclude = "group")
@FieldNameConstants
public class User
        extends AbstractNamedEntityObject<Long>
        implements StatefulObject<String> {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    String state;

    @Column
    String area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    Group group;

    @Column
    String job;

    @Desc("分数")
    Integer score;

    String description;

}
