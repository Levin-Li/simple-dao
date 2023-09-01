package ${modulePackageName}.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;

import org.springframework.boot.autoconfigure.condition.*;
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
public abstract class BaseService {

    @Autowired
    protected SimpleDao simpleDao;

    @Autowired
    protected ApplicationContext applicationContext;

    protected Object selfProxy = null;

    public final String getModuleId() {
        return ModuleOption.ID;
    }

    /**
     * 返回自身的代理
     *
     * @param <T>
     * @return
     */
    protected <T> T getSelfProxy() {
        return (T) getSelfProxy(getClass());
    }

    /**
     * 返回自身的代理
     *
     * @param type
     * @param <T>
     * @return
     */
    protected <T> T getSelfProxy(Class<T> type) {

        if (selfProxy == null
                || !type.isInstance(selfProxy)
                || !(AopUtils.isCglibProxy(selfProxy) || AopUtils.isAopProxy(selfProxy) || AopUtils.isJdkDynamicProxy(selfProxy))) {
            selfProxy = applicationContext.getBean(type);
        }

        return (T) selfProxy;
    }

}
