package ${modulePackageName};

import com.levin.commons.service.domain.EnumDesc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "AppWebMvcConfigurer", matchIfMissing = true)
public class AppWebMvcConfigurer implements WebMvcConfigurer {
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

}
