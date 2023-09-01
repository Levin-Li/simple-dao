package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import com.levin.commons.service.domain.EnumDesc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.config.annotation.*;

/**
 * 模块MVC配置
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
@Configuration(PLUGIN_PREFIX + "${className}")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)
public class ModuleWebMvcConfigurer implements WebMvcConfigurer {

//    @Autowired
//    RbacService rbacService;
//
//    @Autowired
//    AuthService authService;
//
//    @Autowired
//    BizTenantService bizTenantService;

    @PostConstruct
    void init() {
        log.info("init...");
    }

    /**
     * 配置静态访问资源
     * spring boot 默认的{ "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" };
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //{ "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍

        registry.addResourceHandler(ADMIN_UI_PATH + "**")
                .addResourceLocations("classpath:public" + ADMIN_UI_PATH);

        registry.addResourceHandler(H5_UI_PATH + "**")
                .addResourceLocations("classpath:public" + H5_UI_PATH);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping(API_PATH + "**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOriginPatterns("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册拦截器
//        registry.addInterceptor(new DomainInterceptor((domain) -> injectVarService.clearCache(), (className) -> true))
//                .addPathPatterns("/**")
//                .order(Ordered.HIGHEST_PRECEDENCE + 1000);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // configurer.enable();
        // configurer.enable("defaultServletName");

        // 此时会注册一个默认的Handler：DefaultServletHttpRequestHandler，这个Handler也是用来处理静态文件的，它会尝试映射/。当DispatcherServelt映射/时（/ 和/ 是有区别的），并且没有找到合适的Handler来处理请求时，就会交给DefaultServletHttpRequestHandler 来处理。注意：这里的静态资源是放置在web根目录下，而非WEB-INF 下。
    }


    /**
     * 视图配置
     *
     * @param registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {

        //增加 JSP 支持
        // registry.jsp("/WEB-INF/jsp/", ".jsp");
    }

}
