package ${modulePackageName}.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

//Auto gen by simple-dao-codegen ${.now}

/**
 * 抽象服务类
 *
 * @author lilw
 */
@Slf4j
public abstract class BaseService {

    @Resource
    protected SimpleDao simpleDao;

    @Resource
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

}
