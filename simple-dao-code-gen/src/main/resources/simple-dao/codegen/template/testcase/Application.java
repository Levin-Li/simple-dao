package ${modulePackageName};

import com.levin.commons.service.support.*;
import org.springframework.core.env.*;
import org.springframework.beans.factory.annotation.*;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.plugin.support.PluginManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Type;

/**
 *  启动类
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Resource
    Environment environment;

    @Bean
    PluginManager pluginManager() {

        return new PluginManagerImpl() {
            @Override
            public void onApplicationEvent(ContextRefreshedEvent event) {
                log.info("创建自定义的插件管理器-" + getClass().getSimpleName());
                super.onApplicationEvent(event);
            }
        };
    }

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
                 * @param name                变量名
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
