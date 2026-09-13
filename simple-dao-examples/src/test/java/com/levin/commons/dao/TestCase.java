package com.levin.commons.dao;

import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.util.ExprUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

public class TestCase {


    @Test
    public void testExprUtils() {


        String expr = ExprUtils.trimParenthesesPair(" \t \r \n ( ( this.aaa()\r\n\t) )\t\r\n ");

        Assert.isTrue(expr.equals("this.aaa()"), expr);


        expr = ExprUtils.trimParenthesesPair(" \t \r \n ( ( \r\n\t(this.aaa(),aaa) )\t\r\n ");

        Assert.isTrue(expr.equals("(this.aaa(),aaa"), expr);


        expr = ExprUtils.trimParenthesesPair(" \t( \r (\n ( ( this.aaa(),aaa)\r\n)\t) )\t\r)\n ");

        Assert.isTrue(expr.equals("this.aaa(),aaa)"), expr);


    }

}
