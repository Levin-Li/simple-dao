package ${modulePackageName}.resolver;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import ${modulePackageName}.listener.ModuleSpringCacheEventListener;
import ${modulePackageName}.listener.ModuleSpringCacheEventListener.Action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
public class ModuleSpringCacheResolver implements CacheResolver, InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ApplicationContext applicationContext;

    private static final ThreadLocal<CacheOperationInvocationContext<?>> invocationContext = new ThreadLocal<>();

    @AllArgsConstructor
    static class CacheProxy implements Cache {

        Cache delegate;

        Supplier<Collection<ModuleSpringCacheEventListener>> supplier;

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

                Collection<ModuleSpringCacheEventListener> listeners = supplier != null ? supplier.get() : null;

                if (listeners != null) {
                    listeners.forEach(el -> el.onCacheEvent(invocationContext.get(), delegate, action, key, value));
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


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() == applicationContext) {
            event.getApplicationContext().getBeanProvider(ModuleSpringCacheEventListener.class).forEach(ModuleSpringCacheEventListener::add);
        }
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

                ModuleSpringCacheEventListener.cacheMap.computeIfAbsent(cacheName, key -> {

                    Cache cache = getCacheManager().getCache(cacheName);

                    if (cache == null) {
                        throw new IllegalArgumentException("Cannot find cache named '" + cacheName + "' for " + context.getOperation());
                    }

                    return new CacheProxy(cache, () -> ModuleSpringCacheEventListener.eventListeners);

                })

        ).collect(Collectors.toList());

    }

}