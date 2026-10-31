package com.levin.commons.dao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/** 请求对象类级逻辑组的真实数据库测试实体。 */
@Data
@Entity
public class ClassGroupRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orgId;
    private String userId;
    private String state;
}
