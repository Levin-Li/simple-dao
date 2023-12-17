package ${modulePackageName}.resolver;

import static ${modulePackageName}.ModuleOption.*;
import static ${modulePackageName}.entities.EntityConst.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.AbstractCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component(PLUGIN_PREFIX + "ModuleSpringCacheResolver")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleSpringCacheResolver", matchIfMissing = true)
public class ModuleSpringCacheResolver extends AbstractCacheResolver {

    /**
     * Construct a new {@code AbstractCacheResolver}.
     *
     * @see #setCacheManager
     */
    public ModuleSpringCacheResolver() {
    }

    /**
     * Construct a new {@code AbstractCacheResolver} for the given {@link CacheManager}.
     *
     * @param cacheManager the CacheManager to use
     */
    public ModuleSpringCacheResolver(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
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
