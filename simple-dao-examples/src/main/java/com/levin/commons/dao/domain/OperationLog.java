package com.levin.commons.dao.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Accessors(chain = true)
public class OperationLog  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    User user;


    @Lob
    @Column(name = "log_text_FieldName")
    String logText;

    @Temporal(value = TemporalType.TIMESTAMP)
    protected LocalDateTime lastUpdateTime;

}
