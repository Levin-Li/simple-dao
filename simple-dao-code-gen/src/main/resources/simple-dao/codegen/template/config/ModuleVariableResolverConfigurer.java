package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;

import ${modulePackageName}.*;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.IPAddrUtils;
import com.levin.commons.utils.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Configuration(PLUGIN_PREFIX + "ModuleVariableResolverConfigurer")
@Slf4j
@ConditionalOnProperty(value = PLUGIN_PREFIX + "ModuleVariableResolverConfigurer", havingValue = "false", matchIfMissing = true)
public class ModuleVariableResolverConfigurer
        implements VariableResolverConfigurer {


    @Override
    public void config(VariableResolverManager vrm) {

        //@todo
        //加入全局的变量解析器，比如用户数据，环境数据等
        //静态变量
        vrm.add(MapUtils.putFirst("静态变量", "静态变量").build());

        //http相关的动态变量
        vrm.add(httpRequestInfoResolver());

        //动态变量
        vrm.add(new VariableResolver.MapVariableResolver(() -> getContextVars()));
    }


    /**
     * 获取动态变量
     * @return
     */
    protected List<Map<String, Object>> getContextVars() {
        //每次请求都会获取的变量

        return Arrays.asList(MapUtils.putFirst(InjectConsts.ORG_ID, "123456789").build());

    }


    @Bean
    HttpRequestInfoResolver httpRequestInfoResolver() {
        return new HttpRequestInfoResolver();
    }

    static class HttpRequestInfoResolver implements VariableResolver {

        @Autowired
        HttpServletRequest request;

        @Autowired
        HttpServletResponse response;

        @Override
        public <T> ValueHolder<T> resolve(String name, T defaultValue, boolean throwEx, Class<?>... types) throws VariableNotFoundException {

            String value = null;

//            request.getRequestURL() 返回全路径
//            request.getRequestURI() 返回除去host（域名或者ip）部分的路径
//            request.getContextPath() 返回工程名部分，如果工程映射为/，此处返回则为空
//            request.getServletPath() 返回除去host和工程名部分的路径

//            request.getRequestURL() http://localhost:8080/jqueryLearn/resources/request.jsp
//            request.getRequestURI() /jqueryLearn/resources/request.jsp
//            request.getContextPath()/jqueryLearn
//            request.getServletPath()/resources/request.jsp

            if (InjectConsts.IP_ADDR.equalsIgnoreCase(name)) {

                value = IPAddrUtils.try2GetUserRealIPAddr(request);

            } else if (InjectConsts.URL_SERVERNAME.equalsIgnoreCase(name)) {

                value = request.getServerName();

            } else if (InjectConsts.USER_AGENT.equalsIgnoreCase(name)) {

                value = request.getHeader("user-agent");

            } else if (InjectConsts.URL.equalsIgnoreCase(name)) {

                value = request.getRequestURL().toString();

            } else if (InjectConsts.URL_SCHEME.equalsIgnoreCase(name)) {

                value = request.getScheme();

            } else if ("moduleId".equalsIgnoreCase(name)) {

                value = ModuleOption.ID;

            } else {
                return ValueHolder.notValue();
            }

            return new ValueHolder()
                    .setValue(value)
                    .setHasValue(true);
        }
    }

}