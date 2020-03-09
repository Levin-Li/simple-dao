package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@Entity(name = "jpa_dao_test_User")
@Data
@Accessors(chain = true)
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

    @ManyToOne
    @JoinColumn(name = "parent_id")
    Group group;


    Integer score;


    String description;

}
