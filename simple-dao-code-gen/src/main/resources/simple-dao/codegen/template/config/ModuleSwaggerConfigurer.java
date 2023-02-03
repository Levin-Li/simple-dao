package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

//import com.levin.oak.base.autoconfigure.FrameworkProperties;
import com.levin.commons.service.domain.EnumDesc;
import com.levin.commons.service.domain.SignatureReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.*;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

//Swagger3

@Slf4j
//注意：默认不启用
@Configuration(PLUGIN_PREFIX + "ModuleSwaggerConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSwaggerConfigurer", matchIfMissing = true)

@ConditionalOnClass({GroupedOpenApi.class})
public class ModuleSwaggerConfigurer
        implements
//        ModelPropertyBuilderPlugin,
        WebMvcConfigurer {

    public static final String[] SWAGGER_UI_MAPPING_PATHS = {"/swagger-resources/**", "/swagger-ui/**"};

    /**
     * tokenName Authorization
     */
    @Value("${sa-token.token-name:}")
    private String tokenName = "Authorization";

    /**
     * 是否开启swagger
     */
    @Value("${swagger.enabled:true}")
    private boolean enabled;


    /**
     * swagger 枚举值和描述之间的分隔符
     */
    @Value("${swagger.enumDelimiter:}")
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
                .build();
    }

    /**
     * 解决 swagger3   swagger-ui.html 404无法访问的问题
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Stream.of(SWAGGER_UI_MAPPING_PATHS)
//                .filter(p -> !registry.hasMappingForPattern(p))
//                .forEachOrdered(pathPattern ->
//                        registry.addResourceHandler(pathPattern)
//                                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                );
    }

}
