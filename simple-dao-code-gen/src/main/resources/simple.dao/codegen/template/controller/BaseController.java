package ${modulePackageName}.controller;

import com.levin.commons.dao.*;
import com.levin.commons.rbac.*;
import com.levin.commons.utils.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.util.*;

import javax.validation.*;
import java.util.*;
import javax.annotation.*;

import javax.servlet.http.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.support.*;

import javax.validation.constraints.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.*;

import ${modulePackageName}.*;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

/**
 * 抽象控制器
 *
 * @author lilw
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
@Slf4j
//默认需要权限访问
@ResAuthorize(domain = ID, type = BIZ_TYPE_NAME + "-")
@MenuResTag(domain = ID)
public abstract class BaseController {

    @Autowired
    protected HttpServletRequest httpRequest;

    @Autowired
    protected HttpServletResponse httpResponse;

    @Autowired
    protected ApplicationContext applicationContext;

    protected Object selfProxy = null;

    public final String getModuleId() {
        return ModuleOption.ID;
    }

    protected <T> T getSelfProxy(Class<T> type) {

        if (selfProxy == null) {
            selfProxy = applicationContext.getBean(type);
        }

        return (T) selfProxy;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("MM-dd-yyyy"),false));
    }

    /**
     * 获取调用方法名
     *
     * @return
     */
    public static String getInvokeMethodName(int level) {
        return (new Exception()).getStackTrace()[level].getMethodName();
    }

    /**
     * @return
     */
    protected String getContextPath() {
        return httpRequest.getServletContext().getContextPath();
    }

    /**
     * 不支持的操作
     *
     * @param info
     */
    protected void unsupportedOperation(String info) {
        throw new UnsupportedOperationException(StringUtils.hasText(info) ? info : "不支持的操作");
    }

    /**
     * 检查请求
     *
     * @param action
     * @param req
     * @return
     */
    protected <T> T checkRequest(String action, T req) {

        //控制器方法名
        //String methodName = getInvokeMethodName(2);

        return req;
    }

    /**
     * 检查响应
     *
     * @param action
     * @param resp
     * @return
     */
    protected <T> T checkResponse(String action, T resp) {
        return resp;
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
     * 检查结果
     *
     * @param n
     * @param failAction
     * @return
     */
    protected int assertTrue(int n, String failAction) {
        Assert.isTrue(n > 0, failAction);
        return n;
    }

    /**
     * 检查结果
     *
     * @param ok
     * @param failAction
     * @return
     */
    protected boolean assertTrue(boolean ok, String failAction) {
        Assert.isTrue(ok, failAction);
        return ok;
    }
}
