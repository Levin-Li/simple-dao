
package ${modulePackageName}.listener;

import com.levin.commons.service.domain.EnumDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

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

    @Schema(title = "简单监听器")
    @AllArgsConstructor
    class SimpleListener implements ModuleSpringCacheEventListener {

        @Getter
        ModuleSpringCacheEventListener delegate;

        String cacheNamePattern;
        String keyPattern;

        List<Action> actions;

        @Override
        public void onCacheEvent(CacheOperationInvocationContext<?> cacheOperationInvocationContext, Cache cache, Action action, Object key, Object value) {

            if (cacheNamePattern == null || PatternMatchUtils.simpleMatch(cacheNamePattern, cache.getName())) {

                if (keyPattern == null || (key instanceof CharSequence && PatternMatchUtils.simpleMatch(keyPattern, key.toString()))) {

                    if (actions == null || actions.contains(action)) {
                        delegate.onCacheEvent(cacheOperationInvocationContext, cache, action, key, value);
                    }
                }
            }
        }
    }

    /**
     * 增加监听器
     *
     * @param listener
     * @param cacheNamePattern 为 null 匹配所有, 支持*通配符
     * @param keyPattern       为 null 匹配所有，支持*通配符
     * @param actions          为 null 匹配所有
     * @return
     */
    static void add(ModuleSpringCacheEventListener listener, String cacheNamePattern, String keyPattern, Action... actions) {
        Assert.notNull(listener, "listener is null");
        add(new SimpleListener(listener, cacheNamePattern, keyPattern, Arrays.asList(actions)));
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
                    eventListeners.removeIf(l -> l == listener || (l instanceof SimpleListener && ((SimpleListener) l).getDelegate() == listener));
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
