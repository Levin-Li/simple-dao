package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//默认不启用
@Slf4j
//@Configuration(PLUGIN_PREFIX + "ModuleFilterConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleFilterConfigurer", matchIfMissing = true)
public class ModuleFilterConfigurer
        implements
        WebMvcConfigurer {

}
