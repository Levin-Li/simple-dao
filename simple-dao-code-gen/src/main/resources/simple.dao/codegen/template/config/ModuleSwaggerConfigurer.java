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
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

//Swagger3

/**
 * 模块文档配置
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
@Configuration(PLUGIN_PREFIX + "ModuleSwaggerConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSwaggerConfigurer", matchIfMissing = true)
@ConditionalOnClass({GroupedOpenApi.class,})
@Profile({"dev","test","local"})
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
                .addOperationCustomizer((operation, handlerMethod) -> operation)
                .build();
    }

}
