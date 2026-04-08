package com.levin.commons.dao.domain;

import com.levin.commons.dao.Unique;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import jakarta.persistence.*;
import java.io.Serializable;


@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uuid1"})
})
@Data
@Accessors(chain = true)
@FieldNameConstants
public class UniqueTestObj implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String uuid1;

    @Column
    @Unique
    String uuid2;

}
