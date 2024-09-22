package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.utils.DisableApiOperationUtils;

import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//Swagger3

@Slf4j
@Configuration(PLUGIN_PREFIX + "ModuleSwaggerConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSwaggerConfigurer", matchIfMissing = true)
@ConditionalOnClass({GroupedOpenApi.class,})
@Profile({"local", "dev", "test"})
public class ModuleSwaggerConfigurer
        implements
        WebMvcConfigurer {

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
                        //Tag排序
                        openApi
                                //设置模块信息
                                .info(new Info()
                                        .summary(GROUP_NAME)
                                        .title(NAME)
                                        .version(API_VERSION)
                                        .description(DESC)
                                )
                )
                .addOperationCustomizer((operation, handlerMethod) -> {

                    // log.info("{} -- > method: {}", operation.getSummary(), handlerMethod);

                    if (operation != null && (operation.getExtensions() == null || !operation.getExtensions().containsKey("x-order"))) {
                        Long nextOrder = getNextOrder(handlerMethod);
                        operation.addExtension("x-order", nextOrder);
                        operation.addExtension31("order", nextOrder);
                    }

                    return AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), Controller.class)
                            && AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), RequestMapping.class)
                            && DisableApiOperationUtils.isApiEnable(handlerMethod.getBeanType(), handlerMethod.getMethod()) ? operation : null;
                })
                .build();
    }

    private Long getNextOrder(HandlerMethod handlerMethod) {
        return atomicLongMap.computeIfAbsent(handlerMethod.getBeanType().getName(), k -> new AtomicLong(0)).incrementAndGet() * 10;
    }

}
