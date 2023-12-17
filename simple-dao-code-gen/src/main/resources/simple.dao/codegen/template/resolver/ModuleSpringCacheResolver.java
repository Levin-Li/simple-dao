package ${modulePackageName}.resolver;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {

        Collection<String> cacheNames = getCacheNames(context);
        if (cacheNames == null) {
            return Collections.emptyList();
        }

        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            Cache cache = getCacheManager().getCache(cacheName);
            if (cache == null) {
                throw new IllegalArgumentException("Cannot find cache named '" +
                        cacheName + "' for " + context.getOperation());
            }
            result.add(cache);
        }
        return result;
    }

    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {

        Object target = context.getTarget();

        Collection<String> result = Collections.emptySet();

//        if (target instanceof UserService) {
//
//        } else if (target instanceof OrgService) {
//
//        } else if (target instanceof RoleService) {
//
//        }

        //
        if (!result.isEmpty()) {
            result.addAll(context.getOperation().getCacheNames());
        }

        return result.isEmpty() ? context.getOperation().getCacheNames() : result;
    }

}
