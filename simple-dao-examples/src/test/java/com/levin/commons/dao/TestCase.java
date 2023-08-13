package com.levin.commons.dao;

import com.levin.commons.utils.StatHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestCase {

    @Test
    public void testStat() throws InterruptedException {


      long amount= (long) (1.0 * 41980 * 0.6 / 100);




        StatHelper statHelper = new StatHelper();

        while (true) {

            statHelper.onAlarm(15, 100, 0.0, (growthRatio, ration) -> {

                log.warn("Redis消息队列-消费处理速率：{}，环比增长：{}", ration, growthRatio);

            });

            //Thread.sleep(0,100);

        }

    }

}
