package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;


import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import java.util.stream.Stream;

//import springfox.documentation.swagger2.annotations.EnableSwagger2;


//Swagger3
@EnableOpenApi

//@EnableSwagger2

@Slf4j
@Configuration(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)

@ConditionalOnClass({Docket.class})
public class ModuleSwaggerConfigurer implements WebMvcConfigurer {

    /**
     * 是否开启swagger
     */
    @Value("${r"${swagger.enabled:true}"}")
    private boolean enabled;

    @PostConstruct
    void init() {
        log.info("init...");
    }

    @Bean(PLUGIN_PREFIX + "Docket")
    //默认激活的 profile
    @Profile({"dev", "test", "local"})
    public Docket docket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .enable(enabled)
                .groupName(ModuleOption.NAME + "-" + ModuleOption.ID)
                .select()
                //apis： 添加swagger接口提取范围
                .apis(RequestHandlerSelectors.basePackage(PACKAGE_NAME))
                //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
//                .paths(path -> path.startsWith(API_PATH))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("插件[" + ModuleOption.ID +"]接口文档")
                .description("插件[" + ModuleOption.ID + "]接口文档")
                .contact(new Contact("Levin", "https://github.com/Levin-Li/simple-dao", "99668980@qq.com"))
                .version(ModuleOption.VERSION)
                .build();
    }


    /**
     * 解决 swagger3   swagger-ui.html 404无法访问的问题
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Stream.of("/**/swagger-ui/**/*", "/**/springfox-swagger-ui/**/*")
                .filter(p -> !registry.hasMappingForPattern(p))
                .forEachOrdered(pathPattern ->
                        registry.addResourceHandler(pathPattern)
                                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                );
    }

}