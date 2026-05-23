package com.levin.commons.dao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pg_json_append_user")
@Data
@Accessors(chain = true)
public class PgJsonAppendUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_log", columnDefinition = "jsonb")
    private List<ActionLog> actionLog;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> profile;

    @Data
    @Accessors(chain = true)
    public static class ActionLog implements Serializable {

        private String occurTime;

        private String operator;

        private String action;
    }
}
