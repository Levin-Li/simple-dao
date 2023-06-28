package ${modulePackageName}.biz;

import java.util.Map;

/**
 * 注入变量服务
 * @author Auto gen by simple-dao-codegen, @time: ${.now}
 * 代码生成哈希校验码：[]
 */
public interface InjectVarService {

    String INJECT_VAR_CACHE_KEY = InjectVarService.class.getName() + ".INJECT_VAR_CACHE_KEY";

    /**
     * 清除缓存
     */
    void clearCache();

    /**
     * 获取默认常规的注入变量
     *
     * @return
     */
    Map<String, ?> getInjectVars();

}
