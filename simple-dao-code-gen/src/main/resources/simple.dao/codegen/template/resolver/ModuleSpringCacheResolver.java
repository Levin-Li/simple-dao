package ${modulePackageName}.resolver;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import com.levin.commons.service.domain.EnumDesc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 模块缓存解析器
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 *
 */

@Slf4j
@Component(PLUGIN_PREFIX + "ModuleSpringCacheResolver")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSpringCacheResolver", matchIfMissing = true)
public class ModuleSpringCacheResolver implements CacheResolver, InitializingBean {

    @Autowired
    CacheManager cacheManager;

    @Getter
    final Map<String, Cache> cacheList = new ConcurrentHashMap<>();

    @Getter
    final List<CacheEventListener> eventListeners = new ArrayList<>();

    private static final ThreadLocal<CacheOperationInvocationContext<?>> invocationContext = new ThreadLocal<>();

    public enum Action implements EnumDesc {

        @Schema(title = "读取")
        Get,

        @Schema(title = "放入")
        Put,

        @Schema(title = "剔除")
        Evict,

        @Schema(title = "清除所有")
        Clear,

    }

    @FunctionalInterface
    public interface CacheEventListener {
        void onEvent(CacheOperationInvocationContext<?> cacheOperationInvocationContext, Cache cache, Action action, Object key, Object value);
    }

    @AllArgsConstructor
    static class CacheProxy implements Cache {

        Cache delegate;

        Supplier<List<CacheEventListener>> supplier;

        /**
         * Return the cache name.
         */
        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper valueWrapper = delegate.get(key);
            onEvent(Action.Get, key, valueWrapper);
            return valueWrapper;
        }


        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            onEvent(Action.Get, key, value);
            return value;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            T value = delegate.get(key, valueLoader);
            onEvent(Action.Get, key, value);
            return value;
        }

        @Override
        public void put(Object key, Object value) {
            delegate.put(key, value);
            onEvent(Action.Put, key, value);
        }

        @Override
        public void evict(Object key) {
            delegate.evict(key);
            onEvent(Action.Evict, key, null);
        }

        @Override
        public void clear() {
            delegate.clear();
            onEvent(Action.Clear, null, null);
        }

        private void onEvent(Action action, Object key, Object value) {

            try {

                List<CacheEventListener> listeners = supplier != null ? supplier.get() : null;

                if (listeners != null) {
                    listeners.forEach(el -> el.onEvent(invocationContext.get(), delegate, action, key, value));
                }

            } finally {
                invocationContext.remove();
            }
        }
    }

    /**
     * Construct a new {@code AbstractCacheResolver}.
     *
     * @see #setCacheManager
     */
    public ModuleSpringCacheResolver() {
    }


    /**
     * 增加监听器
     *
     * @param listener
     * @return
     */
    public ModuleSpringCacheResolver addCacheEventListener(CacheEventListener listener) {
        return addCacheEventListeners(listener);
    }

    /**
     * 增加监听器
     *
     * @param listeners
     * @return
     */
    public ModuleSpringCacheResolver addCacheEventListeners(CacheEventListener... listeners) {

        if (listeners != null) {
            for (CacheEventListener listener : listeners) {
                if (listener != null) {
                    getEventListeners().add(listener);
                }
            }
        }

        return this;
    }

    /**
     * Set the {@link CacheManager} that this instance should use.
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Return the {@link CacheManager} that this instance uses.
     */
    public CacheManager getCacheManager() {
        Assert.state(this.cacheManager != null, "No CacheManager set");
        return this.cacheManager;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.cacheManager, "CacheManager is required");
    }

    @NotNull
    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {

        Collection<String> cacheNames = context.getOperation().getCacheNames();

        if (cacheNames == null || cacheNames.isEmpty()) {
            return Collections.emptyList();
        }

        invocationContext.set(context);

        return cacheNames.stream().map(cacheName ->

                cacheList.computeIfAbsent(cacheName, key -> {

                    Cache cache = getCacheManager().getCache(cacheName);

                    if (cache == null) {
                        throw new IllegalArgumentException("Cannot find cache named '" + cacheName + "' for " + context.getOperation());
                    }

                    return new CacheProxy(cache, this::getEventListeners);

                })

        ).collect(Collectors.toList());

    }

}