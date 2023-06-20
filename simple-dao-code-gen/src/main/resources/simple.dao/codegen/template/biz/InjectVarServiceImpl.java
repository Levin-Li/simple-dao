package ${modulePackageName}.biz;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;


import com.levin.commons.dao.DaoContext;
import com.levin.commons.dao.SimpleDao;
import com.levin.commons.rbac.RbacRoleObject;
import com.levin.commons.rbac.RbacUserInfo;
import com.levin.commons.service.support.InjectConsts;
import com.levin.commons.service.support.VariableInjector;
import com.levin.commons.service.support.VariableResolverManager;
import com.levin.commons.utils.MapUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;


/**
 * 注入服务
 * 正常情况下，一个项目只需要一个注入服务，为项目提供注入上下文。
 *
 */
//@Service(PLUGIN_PREFIX + "InjectVarService")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "InjectVarService", matchIfMissing = true)
@Slf4j
public class InjectVarServiceImpl implements InjectVarService {

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
            return null;
        }

        @Override
        public String getAvatar() {
            return "anonymous";
        }

        @Override
        public boolean isSuperAdmin() {
            return false;
        }

        @Override
        public String getName() {
            return "anonymous";
        }

        @Override
        public String getTenantId() {
            return null;
        }

        @Override
        public <ID extends Serializable> ID getId() {
            //throw new IllegalStateException("anonymous user");
            return null;
        }
    };

    @Autowired
    Environment environment;

    @Autowired
    SimpleDao dao;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    VariableResolverManager variableResolverManager;

    @PostConstruct
    public void init() {

        //设置上下文
        variableResolverManager.add(VariableInjector.newResolverByMap(() -> Arrays.asList(getInjectVars())));
    }

    /**
     *
     */
    @Override
    public void clearCache() {
        httpServletRequest.removeAttribute(INJECT_VAR_CACHE_KEY);
    }

    @Override
    public Map<String, ?> getInjectVars() {

        //缓存在请求中
        Map<String, ?> result = (Map<String, ?>) httpServletRequest.getAttribute(INJECT_VAR_CACHE_KEY);

        if (result != null) {
            return result;
        }

        MapUtils.Builder<String, Object> builder = MapUtils.newBuilder();

        //@todo  获取当前登录用户
        RbacUserInfo userInfo = null;

        //当前登录用户
        if (userInfo != null) {

            //暂时兼容
            //获取登录信息
            builder.put(InjectConsts.USER_ID, userInfo.getId())
                    .put(InjectConsts.USER_NAME, userInfo.getName())
                    .put(InjectConsts.USER, userInfo)
                    .put(InjectConsts.IS_SUPER_ADMIN, userInfo.isSuperAdmin())
                    .put(InjectConsts.IS_TENANT_ADMIN, userInfo.getRoleList() != null && userInfo.getRoleList().contains(RbacRoleObject.ADMIN_ROLE))
//                    .put(InjectConsts.ORG, userInfo.getOrg())
//                    .put(InjectConsts.ORG_ID, userInfo.getOrgId())
            ;

        } else {
            //匿名用户
            builder.put(InjectConsts.USER, anonymous);
        }


        final Map<String, Object> ctx = builder.build();

        result = ctx;

        //设置注入变量到Dao上下文中
        DaoContext.threadContext.putAll(ctx);

        //缓存到请求对象重
        httpServletRequest.setAttribute(INJECT_VAR_CACHE_KEY, result);

        if (log.isTraceEnabled()) {
            log.trace("getInjectVars ok");
        }

        return result;
    }

}