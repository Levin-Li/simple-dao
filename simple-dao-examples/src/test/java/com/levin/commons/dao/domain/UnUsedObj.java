package com.levin.commons.dao.domain;

import com.levin.commons.annotation.GenNameConstant;

@GenNameConstant
public class UnUsedObj {

    String type;
    String name;

    private transient String transientField;
    private static String staticField;
    private final String finalField = "";



}
