package ${modulePackageName};


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levin.commons.service.support.ValueHolder;
import com.levin.commons.service.support.VariableNotFoundException;
import com.levin.commons.service.support.VariableResolver;
import com.levin.commons.service.support.VariableResolverConfigurer;

import com.levin.commons.utils.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.File;
import java.lang.reflect.Type;
import java.net.BindException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

<#if !enableDubbo>//</#if>import org.apache.dubbo.config.spring.context.annotation.*;

import org.h2.tools.Server;

/**
 *  启动类
 *  @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

<#if !enableDubbo>//</#if>@EnableDubboConfig
@Slf4j
@SpringBootApplication
//@EnableWebSocketMessageBroker
//@EnableWebSocket
@EnableScheduling
@EnableCaching
@EnableAsync
//@EnableDubboConfig
public class Application {

    /**
     * 启动方法
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {

        startH2Server();

        SpringApplication.run(Application.class, args);
    }

    private static void startH2Server() throws InterruptedException, SQLException {

        int h2Port = 9092;

        while (!Thread.currentThread().isInterrupted() && h2Port < 9100) {
            //在项目目录中启动 TCP 服务器
            try {
                Server server = Server.createTcpServer("-tcpPort", "" + h2Port, "-tcpAllowOthers", "-ifNotExists", "-baseDir", new File("").getAbsolutePath()).start();

                System.out.println("***INFO***  H2数据库(支持自动建库)启动成功，URL：" + server.getURL()
                        + "\n\t\t\t可以连接内存数据库和文件数据库，例如：jdbc:h2:" + server.getURL() + "/mem:dev;MODE=MySQL ，jdbc:h2:" + server.getURL() + "/~/dev;MODE=MySQL");

                break;
            } catch (SQLException e) {

                BindException bindException = ExceptionUtils.getCauseByTypes(e, BindException.class);

                //Throwable causedBy = ExceptionUtil.getCausedBy(e, BindException.class);

                if (bindException != null) {
                    System.err.println("***WARN***  H2数据库(支持自动建库)启动失败，端口号冲突，尝试下一个端口号：" + (h2Port + 1));
                } else {
                    throw e;
                }
            }

            Thread.sleep(10);

            h2Port++;
        }
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
        return builder -> {
            builder.cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                    //redis 默认缓存 60 分钟
                    .entryTtl(Duration.of(60, ChronoUnit.MINUTES))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                            new GenericJackson2JsonRedisSerializer(
                                    new ObjectMapper()
                                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
                    )));
        };
    }

    /**
     * redisson 序列化
     *
     * @return
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return config -> config
                .setCodec(new JsonJacksonCodec(getClass().getClassLoader()));
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
            variableResolverManager.add(new VariableResolver() {
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
