package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import com.levin.commons.service.support.AbstractAppDataInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *  应用数据初始化
 *  @author Auto gen by simple-dao-codegen ${.now}
 */

@Component(PLUGIN_PREFIX + "AppDataInitializer")
@Slf4j
public class AppDataInitializer extends AbstractAppDataInitializer {

    @Override
    protected void initData() {
    }

}