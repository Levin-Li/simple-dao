package ${modulePackageName}.config;

import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@Slf4j
public class ModuleWebMvcConfigurer implements WebMvcConfigurer {


    static class OptionsInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 如果是OPTIONS
            if (HttpMethod.OPTIONS.toString().equals(request.getMethod())
                    && request.getRequestURI().startsWith("/" + ModuleOption.API_PATH)) {

                response.setStatus(HttpStatus.NO_CONTENT.value());

                log.debug("跨域配置method:{}, requestURI:{}", request.getMethod(), request.getRequestURI());

            }

            return true;
        }
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

        registry.addResourceHandler("/" + ModuleOption.ADMIN_PATH + "/**")
                .addResourceLocations("classpath:/public/" + ModuleOption.ADMIN_PATH);

        registry.addResourceHandler("/" + ModuleOption.H5_PATH + "/**")
                .addResourceLocations("classpath:/public/" + ModuleOption.H5_PATH);

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/" + ModuleOption.API_PATH + "/**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //处理 OPTIONS 200 请求
        registry.addInterceptor(new OptionsInterceptor());
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