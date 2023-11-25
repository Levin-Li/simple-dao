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
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
//默认不启用
@Slf4j
//@Configuration(PLUGIN_PREFIX + "ModuleFilterConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleFilterConfigurer", havingValue = "true", matchIfMissing = true)
public class ModuleFilterConfigurer
        implements
        WebMvcConfigurer {

}
