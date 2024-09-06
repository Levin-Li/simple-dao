package ${modulePackageName}.services;

import com.levin.commons.utils.ExpressionUtils;
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
import java.math.*;
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
    protected boolean isGte(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) >= 0;
    }

    protected boolean isGtZero(BigDecimal val) {
        return val != null && val.compareTo(BigDecimal.ZERO) > 0;
    }

    protected boolean isNullOrZero(BigDecimal val) {
        return val == null || val.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断对象是否非空，非空字符串，非空集合，非空数组
     *
     * @param obj
     * @return
     */
    protected boolean isNotEmpty(Object obj) {
        return ExpressionUtils.isNotEmpty(obj);
    }

    /**
     *
     * @param obj
     * @return
     */
    protected boolean isEmpty(Object obj) {
        return ExpressionUtils.isEmpty(obj);
    }

    /**
     * 是否有内容
     * @param txt
     * @return
     */
    protected boolean hasText(CharSequence txt){
        return StringUtils.hasText(txt);
    }

    /**
     * 空转默认值
     * @param txt
     * @param defaultValue
     * @return
     * @param <S>
     */
    protected <S extends CharSequence> S empty2Default(S txt, S defaultValue) {
        return StringUtils.hasText(txt) ? txt : defaultValue;
    }

    /**
     * 空转null
     * @param txt
     * @return
     * @param <S>
     */
    protected <S extends CharSequence> S empty2Null(S txt) {
        return StringUtils.hasText(txt) ? txt : null;
    }

    /**
     * null 转空字符串
     * @param txt
     * @return
     */
    protected String null2Empty(String txt) {
        return txt == null ? "" : txt;
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
