package com.levin.commons.dao.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity

@Data
@Accessors(chain = true)
public class B implements Serializable {

    @Id
    @GeneratedValue
    Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id",referencedColumnName = "bid",insertable = false,updatable = false)
    A a;


}
