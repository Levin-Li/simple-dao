package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 模块过滤器
 *
 * @author Auto gen by simple-dao-codegen, @Time: ${.now}
 * 代码生成哈希校验码：[]
 */
//默认不启用
@Slf4j
//@Configuration(PLUGIN_PREFIX + "ModuleFilterConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleFilterConfigurer", matchIfMissing = true)
public class ModuleFilterConfigurer
        implements
        WebMvcConfigurer {

}
