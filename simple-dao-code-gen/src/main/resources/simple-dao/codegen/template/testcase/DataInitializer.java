package ${modulePackageName};

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executor;

<#list entityClassList as clazz>
import ${clazz.name};
</#list>

<#list serviceClassList as className>
import ${className};
</#list>

<#list controllerClassList as className>
import $
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;{className};
</#list>

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.utils.ClassUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executor;

/**
 *  数据初始化
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@Component
@Slf4j
public class DataInitializer implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SimpleDao dao;

    @Autowired
    PluginManager pluginManager;

    @Autowired
    Executor executor;

    @Autowired
    ServerProperties serverProperties;


    @Autowired
    DataSourceProperties dataSourceProperties;

    //spring在启动时候将所有贴有请求映射标签：RequestMapper方法收集起来封装到该对象中
    @Autowired
    RequestMappingHandlerMapping handlerMapping;

    ApplicationContext applicationContext;


    @Autowired
    Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("on applicationContext ...");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext() == applicationContext) {
            initData();
        }

    }

    void initData() {

        log.info("应用 ActiveProfiles:" + Arrays.asList(environment.getActiveProfiles()) + " , 工作目录：[" + new File(".").getAbsolutePath() + "]" + " , 数据库URL：[" + dataSourceProperties.getUrl() + "]");

        String host = Optional.ofNullable(serverProperties.getAddress()).orElse(InetAddress.getLoopbackAddress()).getHostAddress();

        Integer port = Optional.ofNullable(serverProperties.getPort()).orElse(8080);

        String contextPath = Optional.ofNullable(serverProperties.getServlet().getContextPath()).orElse("/");

        String rootUrl = host + ((port == 80) ? "" : (":" + port)) + (contextPath.startsWith("/") ? "" : "/") + contextPath;

        rootUrl = rootUrl.replace("//", "/");

        log.info("***** 服务根路径： http://" + rootUrl);

        boolean haveSwagger = false;//org.springframework.util.ClassUtils.isPresent("springfox.documentation.spring.web.plugins.Docket", getClass().getClassLoader());

        if (haveSwagger) {
            if (applicationContext.getBeanProvider(springfox.documentation.spring.web.plugins.Docket.class).getIfAvailable(() -> null) != null) {
                log.info("***** API文档： http://" + rootUrl + "swagger-ui/index.html");
            } else {
                log.warn("***** 当前项目没有配置 Swagger docket.");
            }
        } else {
            log.info("***** 当前项目没有引入 Swagger 组件，建议引入[io.springfox:springfox-boot-starter]组件");

            getControllerUrls(rootUrl, true);
        }

    }


    /**
     * 获取所有控制器请求方法路径
     *
     * @return
     */
    private List<Map<String, String>> getControllerUrls(String rootUrl, boolean isLog) {

        List<Map<String, String>> resList = new ArrayList<>();

        if (rootUrl == null) {
            rootUrl = "";
        }

        int n = 1;
        //1:获取controller中所有带有@RequestMapper标签的方法
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (RequestMappingInfo requestMappingInfo : handlerMethods.keySet()) {

            //1、获取控制器请求路径

            String controllerMappingUrl = Optional.ofNullable(requestMappingInfo.getPatternsCondition())
                    .map(PatternsRequestCondition::getPatterns)
                    .orElse(Collections.emptySet())
                    .stream()
                    .findFirst()
                    .orElse("");


            RequestMethod requestMethod = Optional.ofNullable(requestMappingInfo.getMethodsCondition())
                    .map(RequestMethodsRequestCondition::getMethods)
                    .orElse(Collections.emptySet())
                    .stream().findFirst()
                    .orElse(null);


            //2、获取方法请求路径
            HandlerMethod handlerMethod = handlerMethods.get(requestMappingInfo);

            // log.debug(" *** handlerMethod: {} , requestMappingInfo: {}", handlerMethod, requestMappingInfo);//{[/beaconArea],methods=[PUT]}

            String methodName = handlerMethod.getMethod().getName();
//
            String methodMappingUrl = "/" + methodName;

            String methodDesc = Arrays.stream(handlerMethod.getMethod().getAnnotations())
                    .filter(annotation -> annotation instanceof Operation)
                    .map(annotation -> ((Operation) annotation).summary())
                    .findFirst().orElse("");
            //获取方法所有注解

            methodMappingUrl = Arrays.stream(handlerMethod.getMethod().getAnnotations())
                    .filter(annotation -> annotation.annotationType().getName().startsWith(GetMapping.class.getPackage().getName()))
                    .map(annotation -> (String[]) ClassUtils.getValue(annotation, "value", false))
                    .filter(values -> values.length > 0)
                    .map(values -> values[0]).findFirst().orElse(methodMappingUrl);

            //3.获取方法注解
            String url = rootUrl + controllerMappingUrl;

            url = "http://" + url.replace("//", "/");

//            System.out.println("完整路径：" + url);
//            System.out.println("方法描述：" + methodDesc);
//            System.out.println("请求类型：" + requestMethod);
            //判断数据库已经存在url是否包含该路径即可

            Map map = new LinkedHashMap<>();

            map.put("resourceUrl", url);

            map.put("resourceName", methodDesc);

            //请求方式（如：GET、POST、PUT、DELETE）
            map.put("resourceMethod", requestMethod);

            map.put("controllerClassName", handlerMethod.getBeanType().getSimpleName());
            map.put("controllerMethodName", methodName);

            //菜单标识  0：非菜单资源   1：菜单资源
//            map.put("menuFlag", 1);
            // 认证等级   0：权限认证   1：登录认证    2：无需认证
//            map.put("authLevel", 1);
            resList.add(map);

            if (isLog) {
                log.info(n++ + " *** 控制器方法({}) {} {} {} ", handlerMethod.getBeanType().getSimpleName() + "." + methodName, methodDesc, requestMethod, url);
            }

        }

        return resList;
    }

}