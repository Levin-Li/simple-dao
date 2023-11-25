package ${modulePackageName};


import com.levin.commons.service.support.*;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.core.env.*;
import org.springframework.beans.factory.annotation.*;

import com.levin.commons.service.support.ValueHolder;
import com.levin.commons.service.support.VariableNotFoundException;
import com.levin.commons.service.support.VariableResolver;
import com.levin.commons.service.support.VariableResolverConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.cache.annotation.EnableCaching;


import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;

//import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;


/**
 *  启动类
 *  @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Slf4j

@SpringBootApplication
//@EnableWebSocketMessageBroker
//@EnableWebSocket
@EnableScheduling
@EnableCaching
@EnableAsync
<#if !enableDubbo>//</#if>@EnableDubboConfig
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    Environment environment;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public BlockingFilter blockingFilter() {
        return new BlockingFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public CorsFilter corsFilter() {

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedMethod(CorsConfiguration.ALL);
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedOriginPattern(CorsConfiguration.ALL);

        config.setMaxAge(18000L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 默认执行器
     *
     * @param builder
     * @return
     */
    @Lazy
    @Bean(name = {"applicationTaskExecutor", "taskExecutor"})
    @ConditionalOnMissingBean(name = {"applicationTaskExecutor", "taskExecutor"})
    public ThreadPoolTaskExecutor applicationTaskExecutor(@Autowired TaskExecutorBuilder builder) {
        return builder.build();
    }

    /**
     * 使用json序列化
     * 默认过期时间
     *
     * @return
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        GenericFastJsonRedisSerializer jsonRedisSerializer = new GenericFastJsonRedisSerializer();
        return builder -> builder
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        //redis 默认缓存 30 分钟
                        .entryTtl(Duration.of(30, ChronoUnit.MINUTES))
                        .disableCachingNullValues()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer)));
    }

    /**
     * redisson 序列化
     * @return
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return config -> config
                .setCodec(JsonJacksonCodec.INSTANCE);
    }

//    @Bean
//    PluginManager pluginManager() {
//        return new PluginManagerImpl() {
//            @Override
//            public void onApplicationEvent(ContextRefreshedEvent event) {
//                log.info("创建自定义的插件管理器-" + getClass().getSimpleName());
//                super.onApplicationEvent(event);
//            }
//        };
//    }

    @Bean
    public VariableResolverConfigurer variableResolverConfigurer() {
        return variableResolverManager -> {

            //加入全局变量
            // variableResolverManager.add(
            //         MapUtils.putFirst(ModuleOption.ID+"_x1", "x1_value")
            //                 .put(ModuleOption.ID+"_x2", "x2_value")
            //                 .build());

            //@todo 增加自定义变量解析器
            //加入
            variableResolverManager.add( new VariableResolver() {
                /**
                 * 获取变量
                 * <p>
                 * 方法必须永远返回一个ValueHolder对象
                 *
                 * @param key                变量名
                 * @param originalValue       原值
                 * @param throwExWhenNotFound 当变量无法解析时是否抛出异常
                 * @param isRequireNotNull
                 * @param expectTypes         期望的类型
                 * @return ValueHolder<T>
                 * @throws VariableNotFoundException 如果变量无法获取将抛出异常
                 */
                @Override
                public <T> ValueHolder<T> resolve(String key, T originalValue, boolean throwExWhenNotFound, boolean isRequireNotNull, Type... expectTypes) throws VariableNotFoundException {

                    if (!key.startsWith("env:")) {
                        return ValueHolder.notValue(key);
                    }

                    key = key.substring(4);

                    return (ValueHolder<T>) new ValueHolder<>()
                            .setValue(environment.getProperty(key))
                            .setHasValue(environment.containsProperty(key));

                }
            });
        };
    }

}
