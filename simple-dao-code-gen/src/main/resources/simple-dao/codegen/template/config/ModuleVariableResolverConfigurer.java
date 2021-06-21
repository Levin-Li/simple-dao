package ${modulePackageName}.config;

import ${modulePackageName}.*;
import com.levin.commons.service.support.VariableResolverConfigurer;
import com.levin.commons.service.support.VariableResolverManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Configuration("${modulePackageName}.config.ModuleVariableResolverConfigurer")
@Slf4j
public class ModuleVariableResolverConfigurer
        implements VariableResolverConfigurer {

    @Override
    public void config(VariableResolverManager variableResolverManager) {

        //@todo
        //加入全局的变量解析器，比如用户数据，环境数据等

    }

}