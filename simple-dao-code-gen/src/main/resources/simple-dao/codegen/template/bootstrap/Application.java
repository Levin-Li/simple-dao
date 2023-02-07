package ${modulePackageName};

import com.levin.commons.service.support.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;
import org.springframework.beans.factory.annotation.*;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.plugin.support.PluginManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Type;

/**
 *  启动类
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@SpringBootApplication
@Slf4j

@EnableScheduling
@EnableCaching
@EnableAsync
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
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
    VariableResolverConfigurer variableResolverConfigurer() {
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
                        return ValueHolder.notValue();
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
