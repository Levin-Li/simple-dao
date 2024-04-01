package ${modulePackageName}.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.*;
import javax.validation.*;
import java.util.*;
import javax.annotation.*;

//import javax.servlet.http.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;
import javax.validation.constraints.*;
import com.levin.commons.dao.*;
import com.levin.commons.dao.support.*;
import com.levin.commons.service.domain.*;

import io.swagger.v3.oas.annotations.*;

import ${modulePackageName}.*;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;


/**
 * 抽象服务类
 *
 * @author lilw
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Slf4j
public abstract class BaseService<S> implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    protected SimpleDao simpleDao;

    @Autowired
    protected ApplicationContext applicationContext;

    protected Object selfProxy = null;

    public final String getModuleId() {
        return ModuleOption.ID;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext() == applicationContext) {
            onApplicationContextReady(applicationContext);
        }
    }

    protected void onApplicationContextReady(ApplicationContext context) {

    }

    /**
     * 返回自身的代理
     *
     * @param <T>
     * @return
     */
    protected <T extends S> T getSelfProxy() {

        if (selfProxy == null) {
            selfProxy = applicationContext.getBean(AopProxyUtils.ultimateTargetClass(this));
        }

        return (T) selfProxy;
    }

}
