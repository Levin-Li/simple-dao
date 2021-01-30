package ${modulePackageName};

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;

<#list serviceClassList as className>
import ${className};
</#list>


@Component
@Slf4j
public class DataInitializer implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SimpleDao dao;

    @Autowired
    PluginManager pluginManager;

    @Autowired
    Executor executor;

    @Autowired
    ServerProperties serverProperties;


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("on applicationContext ...");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext() == applicationContext) {
            initData();
        }

    }

    //    @PostConstruct
    void initData() {

        log.info("on init ...");

        log.info("***** 示例数据初始化完成 ******");


        Integer port = Optional.ofNullable(serverProperties.getPort()).orElse(8080);


        log.info("***** 查询插件： http://127.0.0.1:" + port + "/system/plugin/list");

    }

}
