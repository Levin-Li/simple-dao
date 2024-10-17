package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.utils.DisableApiOperationUtils;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

                    if (operation != null) {

                    }

                    // 过滤掉被 DisableApiOperation 注解禁用的接口
                    return AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), Controller.class)
                            && AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), RequestMapping.class)
                            && DisableApiOperationUtils.isApiEnable(handlerMethod.getBeanType(), handlerMethod.getMethod()) ? operation : null;
                })
                .addOpenApiCustomiser(openApi -> {
                    //删除调没有接口的tag
                    Set<String> tags = openApi.getPaths().values().stream()
                            .flatMap(pathItem -> getOperations(pathItem).stream())
                            .flatMap(operation -> operation.getTags() != null ? operation.getTags().stream() : Stream.empty())
                            .collect(Collectors.toSet());

                    if (openApi.getTags() != null) {
                        //删除调没有接口的tag
                        openApi.getTags().removeIf(tag -> !tags.contains(tag.getName()));
                    }
                })
                .build();
    }

    private List<Operation> getOperations(PathItem pathItem) {
        return Stream.of(pathItem.getOptions(), pathItem.getDelete(), pathItem.getGet(), pathItem.getHead(), pathItem.getTrace()
                , pathItem.getPatch(), pathItem.getPost(), pathItem.getPut()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Long getNextOrder(HandlerMethod handlerMethod) {
        return atomicLongMap.computeIfAbsent(handlerMethod.getBeanType().getName(), k -> new AtomicLong(0)).incrementAndGet() * 10;
    }

}
