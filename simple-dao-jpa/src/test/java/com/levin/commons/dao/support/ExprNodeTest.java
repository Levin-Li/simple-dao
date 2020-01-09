package com.levin.commons.dao.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by echo on 2017/4/29.
 */
public class ExprNodeTest {

    @Test
    public void testToString() throws Exception {

        ExprNode<String, String> root = new ExprNode<>(" AND ", true);

        root.add("a=?")
                .add("b=?")
                .beginGroup(" OR ", true).add("c=?").add("d=?")
                .add("e=?")
                .beginGroup(" AND ", true).add("f=?").add("g=?");


        String expect = "a=?  AND  b=?  AND  (c=?  OR  d=?  OR  e=?  OR  (f=?  AND  g=?))";

        Assert.assertEquals(expect, root.toString());

        System.out.println(root);
    }

}