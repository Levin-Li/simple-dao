package com.levin.commons.dao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "test_string_scope_record")
@Data
public class StringScopeRecord {
    @Id
    private String id;
    private String orgId;
}
