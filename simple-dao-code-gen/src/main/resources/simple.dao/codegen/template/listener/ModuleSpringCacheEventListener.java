
package ${modulePackageName}.listener;


import com.levin.commons.service.domain.EnumDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@FunctionalInterface
public interface ModuleSpringCacheEventListener {

    Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    Set<ModuleSpringCacheEventListener> eventListeners = new LinkedHashSet<>();

    enum Action implements EnumDesc {

        @Schema(title = "读取")
        Get,

        @Schema(title = "放入")
        Put,

        @Schema(title = "剔除")
        Evict,

        @Schema(title = "清除所有")
        Clear,

    }


    /**
     * 增加监听器
     *
     * @param listeners
     * @return
     */
    static void add(ModuleSpringCacheEventListener... listeners) {

        if (listeners != null) {
            for (ModuleSpringCacheEventListener listener : listeners) {
                if (listener != null) {
                    eventListeners.add(listener);
                }
            }
        }

    }

    /**
     * 移除监听器
     *
     * @param listeners
     * @return
     */
    static void remove(ModuleSpringCacheEventListener... listeners) {

        if (listeners != null) {
            for (ModuleSpringCacheEventListener listener : listeners) {
                if (listener != null) {
                    eventListeners.remove(listener);
                }
            }
        }

    }

    /**
     * 缓存事件
     *
     * @param cacheOperationInvocationContext
     * @param cache
     * @param action
     * @param key
     * @param value
     */
    void onCacheEvent(CacheOperationInvocationContext<?> cacheOperationInvocationContext, Cache cache, Action action, Object key, Object value);
}