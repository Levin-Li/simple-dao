package ${modulePackageName}.aspect;

import static  ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.plugin.Plugin;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.support.*;
import com.levin.commons.utils.IPAddrUtils;
import com.levin.commons.utils.MapUtils;
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
                .filter(plugin1 -> className.startsWith(plugin1.getPackageName() + "."))
                .findFirst()
                .orElse(null);

        if (plugin == null) {
            return Collections.emptyList();
        }

        final String packageName = plugin.getPackageName();

        if (!moduleResolverMap.containsKey(packageName)) {

            //放入一个空
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
     *
     * @param joinPoint
     * @throws Throwable
     */
    //@Before("modulePackagePointcut() && controllerPointcut() && requestMappingPointcut()")
    public void injectVar(JoinPoint joinPoint) {

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

        final Map<String, ?> injectVars = injectVarService.getInjectVars();

        variableResolverList.addAll(getModuleResolverList(joinPoint));
        variableResolverList.addAll(variableResolverManager.getVariableResolvers());

        Optional.ofNullable(joinPoint.getArgs()).ifPresent(args ->

                //对方法参数进行迭代
                Arrays.stream(args).filter(Objects::nonNull).forEachOrdered(arg -> {

                    //如果参数本身是一个集合，支持第一级是集合对象的参数
                    Collection<?> params = (arg instanceof Collection) ? ((Collection<?>) arg) : Arrays.asList(arg);

                    params.stream()
                            .filter(Objects::nonNull)
                            .forEachOrdered(param -> {

                                ArrayList<VariableResolver> tempList = new ArrayList<>(variableResolverList.size() + 1);

                                tempList.add(VariableInjector.newResolverByMap(MapUtils.put("_this", param).build(), injectVars));

                                tempList.addAll(variableResolverList);

                                variableInjector.injectByVariableResolvers(param, tempList);
                            });

                })
        );
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
