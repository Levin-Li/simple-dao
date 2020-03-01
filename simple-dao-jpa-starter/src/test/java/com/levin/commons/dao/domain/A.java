package com.levin.commons.dao.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
public class A implements Serializable {


    @Id
    @GeneratedValue
    Integer id;


    @Column
    Integer bid;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid",insertable = false,updatable = false)
    B b;

}
