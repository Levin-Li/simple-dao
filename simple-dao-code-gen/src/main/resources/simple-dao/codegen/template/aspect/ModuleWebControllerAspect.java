package ${modulePackageName}.aspect;

import static  ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.IPAddrUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Aspect
@Slf4j
@Component(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)
public class ModuleWebControllerAspect {

    @Resource
    ApplicationContext context;

    @Resource
    VariableInjector variableInjector;

    @Resource
    VariableResolverManager variableResolverManager;

    @Resource
    HttpServletRequest request;

    @Resource
    HttpServletResponse response;

    @Resource
    ServerProperties serverProperties;

    @PostConstruct
    void init() {
        log.info("init...");
    }

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
     * 拦截例子
     *
     *
     * @param joinPoint
     * @throws Throwable
     */
    //@Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void before(JoinPoint joinPoint) {

    }

    /**
     *
     */
    //@Around("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //得到其方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取方法参数类型数组
        Class[] paramTypes = methodSignature.getParameterTypes();
        String[] paramNames = methodSignature.getParameterNames();

        Method method = methodSignature.getMethod();
        return joinPoint.proceed(joinPoint.getArgs());
    }
}