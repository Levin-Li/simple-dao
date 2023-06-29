package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.entities.*;

import com.levin.commons.service.domain.*;
import com.levin.commons.dao.*;
import com.levin.commons.rbac.*;
import com.levin.commons.dao.repository.*;
import com.levin.commons.plugin.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.context.*;
import org.springframework.util.*;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.*;
import java.util.*;
import java.util.stream.*;


/**
 * 模块插件
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 请不要修改和删除此行内容。
 * 代码生成哈希校验码：[], 请不要修改和删除此行内容。
 */
@Slf4j
@Component(PLUGIN_PREFIX + "${className}")
public class ModulePlugin implements Plugin, PluginManagerAware {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SimpleDao simpleDao;

    private PluginManager pluginManager;


    @Override
    public String getId() {
        return getPackageName();
    }

    @Override
    public String getPackageName() {
        return ModuleOption.PACKAGE_NAME;
    }

    @Override
    public String getName() {
        return ModuleOption.NAME;
    }

    @Override
    public String getVersion() {
        return ModuleOption.VERSION_NAME;
    }

    @Override
    public Map<String, String> getAuthor() {
        return Collections.emptyMap();
    }

    @Override
    public ResLoader getResLoader() {
        //@todo 返回资源加载器
        return resLoader;
    }

    @Override
    public <M extends MenuItem> List<M> getMenuList() {
        return (List<M>) RbacUtils.getMenuItemByController(context, ModuleOption.ID, EntityConst.QUERY_ACTION);
    }

    @Override
    public boolean onEvent(Object... objects) {
        //log.debug(getDescription() + " onEvent " + Arrays.asList(objects));
        //@todo
        return false;
    }

    @Override
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @PostConstruct
    public void init() {
        log.info("plugin init...");
    }

    @Override
    public void destroy() throws PluginException {
    }

    /**
     * 资源加载器
     */
    private final ResLoader resLoader = new ResLoader() {

        final List<SimpleIdentifiable> types = new ArrayList<>();

        final LinkedMultiValueMap<String, Res> resMap = new LinkedMultiValueMap<>();

        @Override
        public List<SimpleIdentifiable> getResTypes() {
            synchronized (types) {
                if (types.isEmpty()) {
                    types.addAll(RbacUtils.loadResTypeFromSpringCtx(context, getPackageName(), null));
                }
            }
            return types;
        }

        @Override
        public <R extends Res> List<R> getResItems(String resType, int loadDeep) {

            Assert.hasText(resType, "资源类型没有指定");

            if (!resMap.containsKey(resType)) {
                resMap.put(resType, RbacUtils.loadResFromSpringCtx(context, getPackageName(), resType));
            }

            return (List<R>) resMap.get(resType);
        }

        @Override
        public <R extends Res> Collection<R> getSubItems(String resType, String resId, int loadDeep) {
            throw new UnsupportedOperationException("getSubItems");
        }

    };

} // end class
