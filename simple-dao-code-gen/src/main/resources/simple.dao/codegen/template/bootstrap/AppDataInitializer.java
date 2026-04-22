package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *  应用数据初始化
 *  @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *  
 */

@Component(PLUGIN_PREFIX + "AppDataInitializer")
@Slf4j
public class AppDataInitializer  {

    @PostConstruct
    public void initData() {
    }

}
