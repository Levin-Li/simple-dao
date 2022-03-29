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

    @Resource(name = HTTP_REQUEST_INFO_RESOLVER)
    HttpRequestInfoResolver httpRequestInfoResolver;


    @Value("${r"$"}{" + PLUGIN_PREFIX + "logHttp:true}")
    boolean enableLog;

    final AtomicBoolean enableHttpLog = new AtomicBoolean(false);

    /**
     * 存储本模块的变量解析器
     */
    private List<VariableResolver> moduleResolverList = new ArrayList<>(7);

    @PostConstruct
    void init() {

        this.enableHttpLog.set(enableLog);

        //增加 HttpRequestInfoResolver
        moduleResolverList.add(httpRequestInfoResolver);

        //只找出本模块的解析器
        List<List<VariableResolver>> resolvers = SpringContextHolder.findBeanByBeanName(context, ResolvableType.forClassWithGenerics(Iterable.class, VariableResolver.class).getType(), PLUGIN_PREFIX);

        resolvers.forEach(moduleResolverList::addAll);

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
     * 变量注入
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void injectVar(JoinPoint joinPoint) {

        if(log.isDebugEnabled()) {
            log.debug("开始为方法 {} 注入变量...", joinPoint.getSignature());
        }

        String headerValue = request.getHeader(PLUGIN_PREFIX + "logHttp");
        if (StringUtils.hasText(headerValue)) {
            enableHttpLog.set(Boolean.TRUE.toString().equalsIgnoreCase(headerValue));
        }
        
        Optional.ofNullable(joinPoint.getArgs()).ifPresent(args -> {
            Arrays.stream(args)
                    .filter(Objects::nonNull)
                    .forEachOrdered(arg -> {
                        variableInjector.injectByVariableResolvers(arg
                                , () -> moduleResolverList
                                , () -> variableResolverManager.getVariableResolvers());
                    });
        });

    }


    /**
     * 记录日志
     */
    @Around("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!enableHttpLog.get() || !log.isDebugEnabled()) {
            return joinPoint.proceed(joinPoint.getArgs());
        }

        LinkedHashMap<String, String> headerMap = new LinkedHashMap<>();

        LinkedHashMap<String, Object> paramMap = new LinkedHashMap<>();

        String requestName = getRequestInfo(joinPoint, headerMap, paramMap);

        log.debug("*** " + requestName + " *** URL: {}?{}, headers:{}, 控制器方法参数：{}"
                , request.getRequestURL(), request.getQueryString()
                , headerMap, paramMap);

        long st = System.currentTimeMillis();

        //动态修改其参数
        //注意，如果调用joinPoint.proceed()方法，则修改的参数值不会生效，必须调用joinPoint.proceed(Object[] args)
        Object result = joinPoint.proceed(joinPoint.getArgs());

        log.debug("*** " + requestName + " *** URL: {}?{}, 执行耗时：{}ms , 响应结果:{}", request.getRequestURL(), request.getQueryString(),
                (System.currentTimeMillis() - st), result);

        //如果这里不返回result，则目标对象实际返回值会被置为null

        return result;

    }


    public String getRequestInfo(ProceedingJoinPoint joinPoint, Map<String, String> headerMap, Map<String, Object> paramMap) throws Throwable {

        //获取方法参数值数组
        Object[] args = joinPoint.getArgs();

        //得到其方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取方法参数类型数组
        Class[] paramTypes = methodSignature.getParameterTypes();
        String[] paramNames = methodSignature.getParameterNames();

        Method method = methodSignature.getMethod();

        String requestName = request.getRequestURI();

        if (method.isAnnotationPresent(Operation.class)) {
            requestName += " " + method.getAnnotation(Operation.class).summary();
        } else if (method.isAnnotationPresent(Schema.class)) {
            requestName += " " + method.getAnnotation(Schema.class).description();
        } else if (method.isAnnotationPresent(Desc.class)) {
            requestName += " " + method.getAnnotation(Desc.class).value();
        }

        if (paramMap != null) {

            if (paramTypes != null && paramTypes.length > 0) {

                if (paramNames == null || paramNames.length != paramTypes.length) {
                    paramNames = new String[paramTypes.length];
                }

                for (int i = 0; i < paramTypes.length; i++) {
                    //  Class paramType = paramTypes[i];

                    String paramName = paramNames[i];

//                    paramName = paramName != null ? paramName : "";
//                    paramName = paramName + "(" + paramType.getSimpleName() + ")";

                    if (StringUtils.hasText(paramName)) {
                        paramMap.put(paramName, args[i]);
                    }
                }
            }
        }


        if (headerMap != null) {

            Enumeration<String> headerNames = request.getHeaderNames();

            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                headerMap.put(key, request.getHeader(key));
            }
        }

        return requestName;
    }

}