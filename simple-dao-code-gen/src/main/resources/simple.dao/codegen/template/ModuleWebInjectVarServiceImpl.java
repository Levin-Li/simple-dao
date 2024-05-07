package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import ${modulePackageName}.biz.InjectVarService;
//import com.levin.commons.dao.DaoContext;
//import com.levin.commons.dao.SimpleDao;
import com.levin.commons.rbac.RbacRoleObject;
import com.levin.commons.rbac.RbacUserInfo;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.MapUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * web模块注入服务
 *
 * 正常情况下，一个项目只需要一个注入服务，为项目提供注入上下文。
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */

//默认不启用
//@Service(PLUGIN_PREFIX + "ModuleWebInjectVarService")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleWebInjectVarService", havingValue = "true", matchIfMissing = true)
@Slf4j
public class ModuleWebInjectVarServiceImpl implements InjectVarService {

    public static final RbacUserInfo anonymous = new RbacUserInfo() {
        @Override
        public String getNickname() {
            return "anonymous";
        }

        @Override
        public String getEmail() {
            return "anonymous@163.com";
        }

        @Override
        public String getTelephone() {
            return "18912345678";
        }

        @Override
        public String getAvatar() {
            return "anonymous";
        }

        @Override
        public String getName() {
            return "anonymous";
        }

        @Override
        public String getTenantId() {
            return "tenant-anonymous";
        }

        @Override
        public <ID extends Serializable> ID getId() {
            //throw new IllegalStateException("anonymous user");
            return  (ID)  "id-anonymous";
        }

        @Override
        public <ID extends Serializable> ID getOrgId() {
            return  (ID)  "org-anonymous";
        }
    };

    @Autowired
    Environment environment;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    VariableResolverManager variableResolverManager;

    @PostConstruct
    public void init() {
        log.info("启用模块Web注入服务...");

        //设置上下文
        //variableResolverManager.add(VariableInjector.newResolverByMap(() -> Arrays.asList(getInjectVars())));

        //变量解析器
        variableResolverManager.add(new VariableResolver() {
            @Override
            public <T> ValueHolder<T> resolve(String name, T originalValue, boolean throwExWhenNotFound, boolean isRequireNotNull, Type... expectTypes) throws VariableNotFoundException {

                //注入变量名称
                if ("xxx".equals(name) ) {
                   //return new ValueHolder(null, name, value);
                }

                return ValueHolder.notValue(throwExWhenNotFound, name);
            }
        }
        );
    }

    /**
     *清除缓存
     *
     */
    @Override
    public void clearCache() {
        httpServletRequest.removeAttribute(INJECT_VAR_CACHE_KEY);
    }

    @Override
    public Map<String, ?> getInjectVars() {

        //如果当前不是web请求，则不注入
        if (RequestContextHolder.getRequestAttributes() == null) {
            return Collections.emptyMap();
        }

        //缓存在请求中
        Map<String, ?> result = (Map<String, ?>) httpServletRequest.getAttribute(INJECT_VAR_CACHE_KEY);

        if (result != null) {
            return result;
        }

        //@todo 设置注入变量
         result = MapUtils.put("xxx","xxx").build();

        //缓存到请求对象重
        httpServletRequest.setAttribute(INJECT_VAR_CACHE_KEY, result);

        if (log.isTraceEnabled()) {
            log.trace("getInjectVars ok");
        }

        return result;
    }

}
