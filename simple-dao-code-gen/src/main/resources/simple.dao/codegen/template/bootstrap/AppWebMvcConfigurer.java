package ${modulePackageName};

import com.levin.commons.service.domain.EnumDesc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.*;
import com.levin.commons.format.DefaultDateFormat;

import javax.annotation.PostConstruct;

import java.util.Date;

import static ${modulePackageName}.ModuleOption.*;

/**
 * 应用MVC配置
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
@Configuration(PLUGIN_PREFIX + "AppWebMvcConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "AppWebMvcConfigurer", havingValue = "true", matchIfMissing = true)
public class AppWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    WebProperties webProperties;

    @PostConstruct
    void init() {
        log.info("init...");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

        //移除默认的转换器
//        registry.removeConvertible(String.class, Enum.class);
//        registry.removeConvertible(int.class, Enum.class);
//        registry.removeConvertible(Integer.class, Enum.class);
//        registry.removeConvertible(Number.class, Enum.class);
//
//        registry.addConverterFactory(EnumDesc.string2EnumFactory);
//        registry.addConverterFactory(EnumDesc.number2EnumFactory);

        registry.removeConvertible(String.class, Date.class);
        registry.removeConvertible(Date.class, String.class);

        registry.addFormatter(new DefaultDateFormat());

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //{ "classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/" };

        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍
        //注意每个资源路径后面的路径加 / !!! 重要的事情说三遍

//        registry.addResourceHandler(ADMIN_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + ADMIN_UI_PATH);
//
//        registry.addResourceHandler(H5_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + H5_UI_PATH);
//
//        registry.addResourceHandler(H5_UI_PATH + "**")
//                .addResourceLocations("classpath:public" + H5_UI_PATH);

        //映射资源目录
        registry.addResourceHandler(("/api-docs/**"))
                .addResourceLocations(webProperties.getResources().getStaticLocations());

    }

}
