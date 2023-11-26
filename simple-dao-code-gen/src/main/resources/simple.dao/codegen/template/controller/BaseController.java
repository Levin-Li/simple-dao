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
@ResAuthorize(domain = ID, type = BIZ_TYPE_NAME)
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

    protected boolean isNotEmpty(Object value) {
        return ExpressionUtils.isNotEmpty(value);
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
     * null2Empty
     * @param txt
     * @return
     */
    protected static String null2Empty(String txt) {
        return null2Empty(txt, "", "");
    }

    /**
     * null2Empty
     * @param txt
     * @param prefix
     * @param suffix
     * @return
     */
    protected static String null2Empty(String txt, String prefix, String suffix) {
        return StringUtils.hasText(txt) ? (prefix + txt + suffix) : "";
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
