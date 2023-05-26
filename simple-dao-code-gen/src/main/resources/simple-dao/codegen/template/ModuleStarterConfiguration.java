package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;

import com.levin.commons.dao.repository.RepositoryFactoryBean;
import com.levin.commons.dao.repository.annotation.EntityRepository;
import com.levin.commons.service.proxy.ProxyBeanScan;

import com.levin.commons.service.support.*;
import com.levin.commons.utils.*;

import javax.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import org.springframework.cloud.openfeign.EnableFeignClients;

//Auto gen by simple-dao-codegen ${.now}

/**
 * 模块自举配置
 *
 * 模块需要自举加载的内容都需要配置在该类中
 *
 */
@Configuration(PLUGIN_PREFIX + "${className}")
@Slf4j

//spring data scan，jpa querydsl entity class ...
@EntityScan({PACKAGE_NAME})

@ComponentScan({PACKAGE_NAME})

@ProxyBeanScan(basePackages = {PACKAGE_NAME} , scanType = EntityRepository.class , factoryBeanClass = RepositoryFactoryBean.class)

@EnableFeignClients({PACKAGE_NAME})

public class ModuleStarterConfiguration {

    @Autowired
    Environment environment;


}
