package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;

import ${modulePackageName}.*;
import com.levin.commons.service.support.VariableResolverConfigurer;
import com.levin.commons.service.support.VariableResolverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


@Configuration(PLUGIN_PREFIX + "ModuleVariableResolverConfigurer")
@Slf4j
@ConditionalOnProperty(value = PLUGIN_PREFIX + "ModuleVariableResolverConfigurer", havingValue = "false", matchIfMissing = true)
public class ModuleVariableResolverConfigurer
        implements VariableResolverConfigurer {

    @Override
    public void config(VariableResolverManager variableResolverManager) {

        //@todo
        //加入全局的变量解析器，比如用户数据，环境数据等

    }

}