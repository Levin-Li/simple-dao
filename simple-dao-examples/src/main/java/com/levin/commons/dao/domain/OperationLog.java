package com.levin.commons.dao.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@Accessors(chain = true)
public class OperationLog  implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    User user;


    @Lob
    @Column(name = "log_text_FieldName")
    String logText;

    @Temporal(value = TemporalType.TIMESTAMP)
    protected Date lastUpdateTime;

}
