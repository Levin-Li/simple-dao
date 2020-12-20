package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_User")
@Data
@Accessors(chain = true)
@ToString(exclude = "group")
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
