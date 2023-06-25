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

import javax.annotation.PostConstruct;

import static ${modulePackageName}.ModuleOption.*;

/**
 * 应用MVC配置
 * 代码生成哈希校验码：[]
 */
@Slf4j
@Configuration(PLUGIN_PREFIX + "AppWebMvcConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "AppWebMvcConfigurer", matchIfMissing = true)
public class AppWebMvcConfigurer implements WebMvcConfigurer {

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

//    static class String2EnumCF implements ConverterFactory<String, Enum> {
//        @Override
//        public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
//            return name -> (T) EnumDesc.parse(targetType, name);
//        }
//    }
//
//    static class Number2EnumCF implements ConverterFactory<Number, Enum> {
//        @Override
//        public <T extends Enum> Converter<Number, T> getConverter(Class<T> targetType) {
//            return code -> (T) EnumDesc.parse(targetType, code.intValue());
//        }
//    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

        //移除默认的转换器
        registry.removeConvertible(String.class, Enum.class);
        registry.removeConvertible(int.class, Enum.class);
        registry.removeConvertible(Integer.class, Enum.class);
        registry.removeConvertible(Number.class, Enum.class);

//        registry.addConverterFactory(new String2EnumCF());
//        registry.addConverterFactory(new Number2EnumCF());

        registry.addConverterFactory(EnumDesc.string2EnumFactory);
        registry.addConverterFactory(EnumDesc.number2EnumFactory);

    }

}
