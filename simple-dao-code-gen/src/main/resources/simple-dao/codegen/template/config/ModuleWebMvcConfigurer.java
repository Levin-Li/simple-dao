package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.config.annotation.*;


@Configuration(PLUGIN_PREFIX + "ModuleWebMvcConfigurer")
@Slf4j
@ConditionalOnProperty(value = PLUGIN_PREFIX + "ModuleWebMvcConfigurer", havingValue = "false", matchIfMissing = true)
public class ModuleWebMvcConfigurer implements WebMvcConfigurer {


    static class CorsOptionsInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

            // 如果是OPTIONS
            //Preflighted requests in CORS
            boolean isCorsPreflightRequest =
                    StringUtils.hasText(request.getHeader("Access-Control-Request-Method"))
                            || StringUtils.hasText(request.getHeader("Access-Control-Request-Headers"));

            if (isCorsPreflightRequest
                    && HttpMethod.OPTIONS.toString().equals(request.getMethod())
                    && request.getRequestURI().startsWith(API_PATH)) {

                //跨域预检请求
                //Access-Control-Max-Age: <delta-seconds>
                //https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request

                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Expose-Headers", "*");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Allow-Methods", "*");

                //Access-Control-Max-Age: <delta-seconds>
                //一周的时间
                response.setHeader("Access-Control-Max-Age", "" + 7 * 24 * 3600);

                response.setStatus(HttpStatus.NO_CONTENT.value());

                log.debug("跨域配置method:{}, requestURI:{}", request.getMethod(), request.getRequestURI());

                return false;

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

        registry.addResourceHandler(ADMIN_PATH + "**")
                .addResourceLocations("classpath:public" + ADMIN_PATH);

        registry.addResourceHandler(H5_PATH + "**")
                .addResourceLocations("classpath:public" + H5_PATH);

        registry.addResourceHandler(H5_PATH + "**")
                .addResourceLocations("classpath:public" + H5_PATH);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping(API_PATH + "**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //处理 OPTIONS 200 请求
        //CORS预检请求处理
        registry.addInterceptor(new CorsOptionsInterceptor());

        //https://sa-token.dev33.cn/
        // 注册路由拦截器，自定义认证规则
//        registry.addInterceptor(new SaRouteInterceptor((req, res, handler)->{
//            // 根据路由划分模块，不同模块不同鉴权
//            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
//            SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
//            SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
//            SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));
//            SaRouter.match("/notice/**", r -> StpUtil.checkPermission("notice"));
//            SaRouter.match("/comment/**", r -> StpUtil.checkPermission("comment"));
//        })).addPathPatterns("/**");


//        registry.addInterceptor(new SaAnnotationInterceptor()).addPathPatterns(API_PATH + "**");

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