package ${modulePackageName}.controller;

import com.levin.commons.rbac.*;
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

//Auto gen by simple-dao-codegen ${.now}


/**
 * 抽象控制器
 *
 * @author lilw
 */
@Slf4j
//默认需要权限访问
@ResAuthorize(domain = ID, type = TYPE_NAME)
@MenuResTag(domain = ID)
public abstract class BaseController {

    @Resource
    protected HttpServletRequest httpRequest;

    @Resource
    protected HttpServletResponse httpResponse;

    @Resource
    protected ApplicationContext applicationContext;

    protected Object selfProxy = null;

    public final String getModuleId() {
        return ModuleOption.ID;
    }

    protected boolean isNotEmpty(Object value) {
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }

    protected <T> T getSelfProxy(Class<T> type) {

        if (selfProxy == null) {
            selfProxy = applicationContext.getBean(type);
        }

        return (T) selfProxy;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder){
       // binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("MM-dd-yyyy"),false));
    }

}
