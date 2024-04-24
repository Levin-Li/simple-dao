package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.service.domain.DisableApiOperation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.HandlerMethod;
import org.springframework.beans.factory.annotation.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

//Swagger3

/**
 * 模块文档配置
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
@Configuration(PLUGIN_PREFIX + "ModuleSwaggerConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSwaggerConfigurer", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({GroupedOpenApi.class,})
@Profile({"dev","test","local"})
public class ModuleSwaggerConfigurer{

    /**
     * tokenName Authorization
     */
    @Value("${r"${sa-token.token-name:}"}")
    private String tokenName = "Authorization";

    /**
     * swagger 枚举值和描述之间的分隔符
     */
    @Value("${r"${swagger.enumDelimiter:}"}")
    private String enumDelimiter;

//    @Autowired
//    FrameworkProperties frameworkProperties;

    @Autowired
    Environment environment;

    final Map<String, AtomicLong> atomicLongMap = new ConcurrentHashMap<>();

    private static final String GROUP_NAME = ModuleOption.NAME + "-" + ModuleOption.ID;

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(enumDelimiter)) {
            enumDelimiter = "--";
        }

        log.info("init...");
    }

    @Bean(PLUGIN_PREFIX + "GroupedOpenApi")
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group(ID)
                .displayName(GROUP_NAME)
                .packagesToScan(PACKAGE_NAME)
                .addOpenApiCustomiser(openApi ->
                        openApi.setInfo(new Info()
                                .summary(GROUP_NAME)
                                .title(NAME)
                                .version(API_VERSION)
                                .description(DESC)
                        ))
                .addOperationCustomizer((operation, handlerMethod) -> {

                    // log.info("{} -- > method: {}", operation.getSummary(), handlerMethod);

                    if (operation != null && (operation.getExtensions() == null || !operation.getExtensions().containsKey("x-order"))) {
                        Long nextOrder = getNextOrder(handlerMethod);
                        operation.addExtension("x-order", nextOrder);
                        operation.addExtension31("order", nextOrder);
                    }

                    return isApiEnable(handlerMethod.getBeanType(), handlerMethod.getMethod()) ? operation : null;
                })
                .build();
    }

    private Long getNextOrder(HandlerMethod handlerMethod) {
        return atomicLongMap.computeIfAbsent(handlerMethod.getBeanType().getName(), k -> new AtomicLong(0)).incrementAndGet() * 10;
    }


    private boolean isApiEnable(Class<?> beanType, Method method) {

        if (beanType == null) {
            beanType = method.getDeclaringClass();
        }

        DisableApiOperation disableApi = AnnotatedElementUtils.findMergedAnnotation(method, DisableApiOperation.class);
        final Operation operation = AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);

        if (disableApi != null) {
            return false;
        }

        disableApi = AnnotatedElementUtils.findMergedAnnotation(beanType, DisableApiOperation.class);

        if (disableApi != null && disableApi.value() != null) {
            if (Stream.of(disableApi.value()).filter(StringUtils::hasText).anyMatch(
                    txt -> txt.equals(method.getName())
                            || txt.equals(method.toGenericString())
                            || txt.equals(operation != null ? operation.method() : null)
                            || txt.equals(operation != null ? operation.operationId() : null)
                            || PatternMatchUtils.simpleMatch(txt, operation != null ? operation.summary() : null)
            )
            ) {
                return false;
            }
        }

        return true;
    }

}
