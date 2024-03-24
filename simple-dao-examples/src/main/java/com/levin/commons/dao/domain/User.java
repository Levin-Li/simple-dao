package com.levin.commons.dao.domain;

import com.levin.commons.dao.domain.support.AbstractNamedEntityObject;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.PrimitiveArrayJsonConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

/**
 * Created by echo on 2015/11/17.
 */
@EqualsAndHashCode(callSuper = true)
@Entity()
@Table(name = "JpaDaoTestUser")
@Data
@Accessors(chain = true)
@ToString(exclude = "group")
@FieldNameConstants
public class User
        extends AbstractNamedEntityObject
        implements StatefulObject  {

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

    @Schema(description = "归属的虚拟组织")
    @InjectVar(converter = PrimitiveArrayJsonConverter.class)
    String belongOrgList;

}
