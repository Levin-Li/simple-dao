package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import ${modulePackageName}.biz.InjectVarService;
//import com.levin.commons.dao.DaoContext;
//import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.service.support.*;

import com.levin.commons.rbac.RbacRoleObject;
import com.levin.commons.rbac.RbacUserInfo;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.MapUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * web模块注入服务
 * <p>
 * 正常情况下，一个项目只需要一个注入服务，为项目提供注入上下文。
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */

//默认不启用
//@Service(PLUGIN_PREFIX + "ModuleWebInjectVarService")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleWebInjectVarService", havingValue = "true", matchIfMissing = true)
@Slf4j
public class ModuleWebInjectVarServiceImpl implements InjectVarService {

    @Autowired
    Environment environment;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    VariableResolverManager variableResolverManager;

    @Autowired
    PluginManager pluginManager;

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
                                            if ("xxx".equals(name)) {
                                                //return new ValueHolder(null, name, value);
                                            }

                                            return ValueHolder.notValue(throwExWhenNotFound, name);
                                        }
                                    }
        );
    }

    protected boolean isWebContext() {
        return RequestContextHolder.getRequestAttributes() != null;
    }

    public void clearCache(){
        clearCache(null);
    }

    /**
     * 清除缓存
     */

    public void clearCache(Object context) {

        ServletRequest request = (context instanceof ServletRequest) ? (ServletRequest) context : (isWebContext() ? httpServletRequest : null);

        if (request != null) {
            request.removeAttribute(INJECT_VAR_CACHE_KEY);
        }

    }


    public List<String> getBizStack(Thread thread) {

        if (thread == null) {
            thread = Thread.currentThread();
        }

        Collection<Plugin> plugins = pluginManager.getInstalledPlugins();

        return Stream.of(thread.getStackTrace())

                //过滤自己
                .filter(e -> !e.getClassName().startsWith(getClass().getName()))
                .filter(e -> !e.getClassName().startsWith(InjectVarService.class.getName()))

                //只过滤出业务类
                .filter(e -> plugins.stream().anyMatch(plugin -> e.getClassName().startsWith(plugin.getPackageName())))

                .map(e -> e.getClassName() + ":" + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")")

                .collect(Collectors.toList());
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
        //注入当前登录用户
        result = MapUtils.put("xxx", "xxx").build();

        //缓存到请求对象重
        httpServletRequest.setAttribute(INJECT_VAR_CACHE_KEY, result);

        if (log.isTraceEnabled()) {
            log.trace("getInjectVars ok");
        }

        return result;
    }

}
