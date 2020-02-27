package com.levin.commons.dao.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QLUtilsTest {

    @Test
    public void parseSelectColumns() {


        List<String[]> strings = QLUtils.parseSelectColumns(null, "a.bb AS abb,c.bb AS cbb,d.bb");

        System.out.println(strings);

    }
}