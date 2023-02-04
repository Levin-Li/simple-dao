package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

//Swagger3

@Slf4j
@Configuration(PLUGIN_PREFIX + "ModuleSwaggerConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSwaggerConfigurer", matchIfMissing = true)
@ConditionalOnClass({GroupedOpenApi.class,})
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

//    @Resource
//    FrameworkProperties frameworkProperties;

    @Resource
    Environment environment;

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
                                .version(VERSION)
                                .description(GROUP_NAME)
                        ))
                .addOperationCustomizer((operation, handlerMethod) -> operation)
                .build();
    }

}
