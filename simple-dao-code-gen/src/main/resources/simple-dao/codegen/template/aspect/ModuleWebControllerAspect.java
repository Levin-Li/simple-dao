
package ${modulePackageName}.aspect;

import ${modulePackageName}.*;

import com.levin.commons.service.support.*;
import com.levin.commons.utils.IPAddrUtils;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import org.aspectj.lang.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


@Aspect
@Component("${modulePackageName}.aspect.ModuleWebControllerAspect")
@Slf4j
public class ModuleWebControllerAspect {

    @Autowired
    VariableInjector variableInjector;

    @Autowired
    VariableResolverManager variableResolverManager;

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    final VariableResolver httpRequestInfoResolver = new VariableResolver() {
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
    };

    /**
     * 模块包
     */
    @Pointcut("execution(* ${modulePackageName}..*.*(..))")
    public void modulePackagePointcut() {
    }

    /**
     * 控制器
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)" +
            " || @within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {
    }

    /**
     * 请求映射方法
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)"
    )
    public void requestMappingPointcut() {
    }


    /**
     * 变量注入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void injectVar(JoinPoint joinPoint) {

        log.debug("开始为方法 {} 注入变量...", joinPoint.getSignature());

        //加入线程级别的http请求解析器，线程级别解析器会被优先使用
        variableResolverManager.addVariableResolvers(true, httpRequestInfoResolver);

        Optional.ofNullable(joinPoint.getArgs()).ifPresent(args -> {
            Arrays.stream(args)
                    .filter(Objects::nonNull)
                    .forEachOrdered(arg -> {
                        variableInjector.injectByVariableResolver(arg
                                , () -> variableResolverManager.getVariableResolvers(true)
                                , () -> variableResolverManager.getVariableResolvers(false));
                    });
        });

    }

    /**
     * 记录日志
     */
    //@Around("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取方法参数值数组
        Object[] args = joinPoint.getArgs();

        //得到其方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        //获取方法参数类型数组
        Class[] paramTypeArray = methodSignature.getParameterTypes();

        log.info("请求参数为{}", args);

        //动态修改其参数
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object result = joinPoint.proceed(args);

        log.info("响应结果为{}", result);

        //如果这里不返回result，则目标对象实际返回值会被置为null

        return result;
    }

}