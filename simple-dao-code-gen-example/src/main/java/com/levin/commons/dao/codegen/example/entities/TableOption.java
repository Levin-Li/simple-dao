package com.levin.commons.dao.codegen.example.entities;

/**
 * module table prefix
 *
 * eg.
 *
 * //@Entity(name = ModuleTableOption.PREFIX + "exam_tasks")
 * //@Table(name = ModuleTableOption.PREFIX + "exam_tasks")
 * //Auto gen by simple-dao-codegen Tue Mar 02 10:13:56 CST 2021
 *
 */
public interface TableOption {

    /**
     * JPA/Hibernate table name prefix
     */
    String PREFIX = "com.levin.commons.dao.codegen.example-";

}
