package com.levin.commons.dao;

import cn.hutool.core.lang.Assert;
import com.levin.commons.dao.util.ExprUtils;
import com.levin.commons.utils.StatHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

@Slf4j
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

    @Test
    public void testStat() throws InterruptedException {


        long amount = (long) (1.0 * 41980 * 0.6 / 100);


        String trimmed = StringUtils.trimWhitespace(" \r \n \t  , \r \n \t ");

        Assert.isTrue(trimmed.equals(","));

        StatHelper statHelper = new StatHelper();

        while (true) {

            statHelper.onAlarm(15, 100, 0.0, (growthRatio, ration) -> {

                log.warn("Redis消息队列-消费处理速率：{}，环比增长：{}", ration, growthRatio);

            });

            //Thread.sleep(0,100);

        }

    }

}
