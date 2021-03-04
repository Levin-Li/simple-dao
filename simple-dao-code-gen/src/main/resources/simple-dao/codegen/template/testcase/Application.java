package ${modulePackageName};

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

}
