package ${modulePackageName}.aspect;

import static  ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import cn.hutool.core.lang.Assert;
import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.MapUtils;
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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

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

    @Autowired
    ServerProperties serverProperties;

    @Autowired
    PluginManager pluginManager;

    /**
     * 存储模块的变量解析器
     */
    private MultiValueMap<String, VariableResolver> moduleResolverMap = new LinkedMultiValueMap<>();

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
     * @param joinPoint
     * @throws Throwable
     */
    //@Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void before(JoinPoint joinPoint) {

    }

    /**
     * 获取JoinPoint所在模块的变量解析器
     *
     * @param joinPoint
     * @return
     */
    private List<VariableResolver> getModuleResolverList(JoinPoint joinPoint) {

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

//            Supplier<List<Map<String, ?>>>

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
     * AOP变量注入
     * 默认是不启用的
     *
     * @param joinPoint
     * @throws Throwable
     */
    //@Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void injectVar(JoinPoint joinPoint) {

        final Object[] requestArgs = joinPoint.getArgs();
        //如果没有参数,或是空参数 或是简单参数，直接返回
        if (requestArgs == null || requestArgs.length == 0
                || Arrays.stream(requestArgs).allMatch(arg -> arg == null || BeanUtils.isSimpleValueType(arg.getClass()))) {
            return;
        }

        final String path = getRequestPath();

        Signature signature = joinPoint.getSignature();

        final String className = signature.getDeclaringTypeName();

        if (className.startsWith("springfox.")
                || className.startsWith("org.springdoc.")) {
            return;
        }

        //去除应用路径后，进行匹配
        if (path.equals(serverProperties.getError().getPath())) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("开始为方法 {} 注入变量...", signature);
        }

        final List<VariableResolver> variableResolverList = new ArrayList<>();

        //@todo 设计一个注入服务
        final Map<String, ?> injectVars = Collections.emptyMap(); // injectVarService.getInjectVars();

        variableResolverList.addAll(getModuleResolverList(joinPoint));
        variableResolverList.addAll(variableResolverManager.getVariableResolvers());

        //对方法参数进行迭代
        Arrays.stream(requestArgs)
                .filter(Objects::nonNull)
                .filter(arg -> !BeanUtils.isSimpleValueType(arg.getClass()))
                .forEachOrdered(arg -> {

                    //如果参数本身是一个集合，支持第一级是集合对象的参数
                    Collection<?> params = (arg instanceof Collection) ? ((Collection<?>) arg) : Arrays.asList(arg);

                    params.stream()
                            .filter(Objects::nonNull)
                            .forEachOrdered(param -> {

                                ArrayList<VariableResolver> tempList = new ArrayList<>(variableResolverList.size() + 1);

                                tempList.add(VariableInjector.newResolverByMap(param, MapUtils.put("_this", param).build(), injectVars));

                                tempList.addAll(variableResolverList);

                                variableInjector.injectValues(param, tempList);
                            });

                });

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
