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

    @Autowired
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
                @Override
                public <T> ValueHolder<T> resolve(String key, T oldValue, boolean required, Class<?>... classes) throws VariableNotFoundException {

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
