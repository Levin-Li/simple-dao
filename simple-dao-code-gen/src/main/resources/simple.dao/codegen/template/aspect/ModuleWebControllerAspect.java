package ${modulePackageName}.aspect;

import static  ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import cn.hutool.core.lang.Assert;
import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.service.exception.AccessDeniedException;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.MapUtils;
import com.levin.commons.service.domain.DisableApiOperation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 模块控制器切面拦截器
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */
@Aspect
@Slf4j
@Component(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", havingValue = "true", matchIfMissing = true)
public class ModuleWebControllerAspect {

    @Autowired
    ApplicationContext context;

    @Autowired
    VariableInjector variableInjector;

    @Autowired
    VariableResolverManager variableResolverManager;

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

//    @Autowired
//    FrameworkProperties frameworkProperties;

    @Autowired
    ServerProperties serverProperties;

    @Autowired
    PluginManager pluginManager;

    /**
     * 存储模块的变量解析器
     */
    private final MultiValueMap<String, VariableResolver> moduleResolverMap = new LinkedMultiValueMap<>();

    private boolean isInit = false;


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
     * 获取JoinPoint所在模块的变量解析器
     *
     * @param joinPoint
     * @return
     */
    private List<VariableResolver> getModuleResolverList(JoinPoint joinPoint) {

        Assert.isTrue(isInit, "系统初始化还未完成");

        Signature signature = joinPoint.getSignature();

        final String className = signature.getDeclaringTypeName();

        //获取当前插件
        Plugin plugin = pluginManager.getInstalledPlugins()
                .stream()
                //找出类归属的插件
                .filter(p -> className.startsWith(p.getPackageName() + "."))
                .findFirst()
                .orElse(null);

        if (plugin == null) {
            log.warn("AOP拦截，类：{} --> 模块：未知, signature:{}", className, signature);
        }

        if (plugin == null) {
            return Collections.emptyList();
        }

        return getVariableResolvers(plugin.getPackageName());
    }

    private synchronized List<VariableResolver> getVariableResolvers(String packageName) {

        //final String packageName = plugin.getPackageName();

        //如果当前模块不存在解析器
        if (!moduleResolverMap.containsKey(packageName)) {

            //放入一个空，防止解析变量出现错误
            moduleResolverMap.addAll(packageName, Collections.emptyList());

            //按bean名查找 List<VariableResolver> bean
            SpringContextHolder.<List<VariableResolver>>findBeanByBeanName(context
                            , ResolvableType.forClassWithGenerics(Iterable.class, VariableResolver.class).getType()
                            , "plugin." + packageName, packageName)
                    .forEach(list -> moduleResolverMap.addAll(packageName, list));

            //按bean名查找 VariableResolver bean
            SpringContextHolder.<VariableResolver>findBeanByBeanName(context
                            , ResolvableType.forClass(VariableResolver.class).getType()
                            , "plugin." + packageName, packageName)
                    .stream().filter(Objects::nonNull)
                    .forEach(v -> moduleResolverMap.add(packageName, v));


            //按包名查找
//            SpringContextHolder.<VariableResolver>findBeanByPkgName(context
//                    , ResolvableType.forClass(VariableResolver.class).getType()
//                    , packageName)
//                    .forEach(v -> moduleResolverMap.add(packageName, v));


        }

        return moduleResolverMap.getOrDefault(packageName, Collections.emptyList());

    }


    /**
     * 变量注入
     * <p>
     * 目前对所有的路径都进行拦截处理
     *
     * @param joinPoint
     * @throws Throwable
     */
//    @Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    @Order(0)
//    @Around("modulePackagePointcut() && controllerPointcut()")
    public Object injectVar(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        final String className = signature.getDeclaringTypeName();

        if (className.startsWith("springfox.")
                || className.startsWith("org.springframework.")
                || className.startsWith("org.springdoc.")
        ) {
            return joinPoint.proceed();
        }

        //checkDisableApi(joinPoint, signature);

        final String path = getRequestPath();
        //去除应用路径后，进行匹配
        if (path.equals(serverProperties.getError().getPath())
            //    || !frameworkProperties.getInject().isMatched(className, path)
        ) {
            return joinPoint.proceed();
        }

        //如果不是请求映射方法，直接返回
        if (!AnnotatedElementUtils.hasAnnotation(signature.getMethod(), RequestMapping.class)) {
            return joinPoint.proceed();
        }

        try {
            List<VariableResolver> variableResolvers = tryInjectVar(joinPoint);

            VariableInjector.setVariableResolversForCurrentThread(variableResolvers);

            return log(joinPoint);

        } finally {
            VariableInjector.setVariableResolversForCurrentThread(null);
        }
    }

    private static void checkDisableApi(ProceedingJoinPoint joinPoint, MethodSignature signature) {

        Method method = signature.getMethod();

        Object target = joinPoint.getTarget() == null ? joinPoint.getThis() : joinPoint.getTarget();

        if (isApiEnable(target != null ? target.getClass() : method.getDeclaringClass(), method)) {
            throw new AccessDeniedException("不可访问的API");
        }
    }

    public static boolean isApiEnable(Class<?> beanType, Method method) {

        if (beanType == null) {
            beanType = method.getDeclaringClass();
        }

        DisableApiOperation disableApi = AnnotatedElementUtils.findMergedAnnotation(method, DisableApiOperation.class);

        if (disableApi != null) {
            return false;
        }

        disableApi = AnnotatedElementUtils.findMergedAnnotation(beanType, DisableApiOperation.class);

        if (disableApi != null && (disableApi.value().length > 0 || disableApi.excludes().length > 0)) {

            final Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
            final RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);


            Predicate<String[]> predicate = patterns -> Stream.of(patterns).filter(StringUtils::hasText).map(String::trim).anyMatch(
                    txt -> txt.equals(method.getName())
                            || txt.equals(method.toGenericString())
                            || txt.equals(operation != null ? operation.method() : null)
                            || txt.equals(operation != null ? operation.operationId() : null)
                            || PatternMatchUtils.simpleMatch(txt, operation != null ? operation.summary() : null)
                            || txt.toLowerCase().startsWith("path:") && requestMapping != null && Stream.of(requestMapping.value()).filter(StringUtils::hasText).anyMatch(path -> PatternMatchUtils.simpleMatch(txt.substring("path:".length()), path))
            );

            //如果匹配禁止的
            if (disableApi.value().length > 0
                    && predicate.test(disableApi.value())) {
                return false;
            }

            //如果匹配排除的
            if (disableApi.excludes().length > 0) {
                return predicate.test(disableApi.excludes());
            }

        }

        return true;
    }
    public List<VariableResolver> tryInjectVar(ProceedingJoinPoint joinPoint) {

        final List<VariableResolver> variableResolverList = new ArrayList<>();
        final Map<String, ?> injectVars = Collections.emptyMap();// injectVarService.getInjectVars();

        List<VariableResolver> moduleResolverList = getModuleResolverList(joinPoint);

        if (moduleResolverList != null && !moduleResolverList.isEmpty()) {
            variableResolverList.addAll(moduleResolverList);
        }

        variableResolverList.addAll(variableResolverManager.getVariableResolvers());

        Object[] requestArgs = joinPoint.getArgs();

        //如果没有参数,或是空参数 或是简单参数，直接返回
        if (requestArgs == null
                || requestArgs.length == 0
                || Arrays.stream(requestArgs).allMatch(arg -> arg == null || BeanUtils.isSimpleValueType(arg.getClass()))) {
            return variableResolverList;
        }

        final String path = getRequestPath();

        Signature signature = joinPoint.getSignature();

        final String className = signature.getDeclaringTypeName();

        if (log.isDebugEnabled()) {
            log.debug("开始为方法 {} 注入变量...", signature);
        }

        if (moduleResolverList == null || moduleResolverList.isEmpty()) {
            log.warn("AOP拦截，类：{}，URI:{}, signature:{} 没有找到模块变量解析器", className, path, signature);
        }

        //对方法参数进行迭代
        Arrays.stream(requestArgs)
                //不是Null的参数
                .filter(Objects::nonNull)
                //不是简单类型的参数
                .filter(arg -> !BeanUtils.isSimpleValueType(arg.getClass()))
                .forEachOrdered(arg -> {

                    //如果参数本身是一个集合，支持第一级是集合对象的参数
                    Collection<?> params = (arg instanceof Collection) ? ((Collection<?>) arg) : Arrays.asList(arg);

                    params.stream()
                            .filter(Objects::nonNull)
                            .forEachOrdered(param -> {

                                ArrayList<VariableResolver> tempList = new ArrayList<>(variableResolverList.size() + 1);

                                tempList.add(VariableInjector.newResolverByMap(MapUtils.put("_this", param).build(), injectVars));

                                tempList.addAll(variableResolverList);

                                try {
                                    VariableInjector.setVariableResolversForCurrentThread(tempList);
                                    variableInjector.injectValues(param, tempList);
                                } finally {
                                    VariableInjector.setVariableResolversForCurrentThread(null);
                                }
                            });

                });

        return variableResolverList;
    }

    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    /**
     * 获取当前请求路径
     *
     * @return
     */
    private String getRequestPath() {

        String contextPath = serverProperties.getServlet().getContextPath() + "/";

        contextPath = contextPath.replace("//", "/");

        String path = request.getRequestURI().replace("//", "/");

        //去除应用路径
        if (path.startsWith(contextPath)) {
            path = path.substring(contextPath.length() - 1);
        }

        return path;
    }

    /**
     * @Around注解用于修饰Around增强处理，Around增强处理是功能比较强大的增强处理，它近似于Before增强处理和AfterReturing增强处理的总结，Around增强处理既可在执行目标方法之前增强动作，也可在执行目标方法之后织入增强的执行。与Before增强处理、AfterReturning增强处理不同的是，Around增强处理可以决定目标方法在什么时候执行，如何执行，甚至可以完全阻止目标方法的执行。
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
