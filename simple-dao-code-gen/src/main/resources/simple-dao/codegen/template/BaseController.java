package ${modulePackageName}.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import ${modulePackageName}.*;

import static ${modulePackageName}.ModuleOption.*;

//Auto gen by simple-dao-codegen ${.now}


/**
 * 抽象控制器
 *
 * @author lilw
 */
@Slf4j
public abstract class BaseController {

    @Resource
    protected HttpServletRequest httpRequest;

    @Resource
    protected HttpServletResponse httpResponse;

    public final String getModuleId() {
        return ModuleOption.ID;
    }

    protected boolean isNotEmpty(Object value) {
        return value != null
                && (!(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value));
    }
}
