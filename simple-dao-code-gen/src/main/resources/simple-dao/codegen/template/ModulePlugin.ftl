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

//Auto gen by simple-dao-codegen ${.now}
//模块插件

@Slf4j
@Component(PLUGIN_PREFIX + "ModulePlugin")
<#--public class ${camelStyleModuleName}Plugin implements Plugin, PluginManagerAware {-->
public class ModulePlugin implements Plugin, PluginManagerAware {

    @Resource
    ApplicationContext context;

    @Autowired
    SimpleDao simpleDao;

    final String pid = ModuleOption.ID;

    private PluginManager pluginManager;


    private final ResLoader resLoader = new ResLoader() {

        final List<Identifiable> types = new LinkedList<>();

        final List<Res> pluginResList = new LinkedList<>();

        @Override
        public List<Identifiable> getResTypes() {
            synchronized (types) {
                if (types.isEmpty()) {
                    types.addAll(RbacUtils.loadResTypeFromSpringCtx(context, getId(), null));
                }
            }
            return types;
        }


        @Override
        public <R extends Res> Collection<R> getResItems(String resType, int loadDeep) {

            Assert.hasText(resType, "资源类型没有指定");

            synchronized (pluginResList) {
                if (pluginResList.isEmpty()) {
                    pluginResList.addAll(RbacUtils.loadResFromSpringCtx(context, getId(), resType));
                }
            }

            return (Collection<R>) pluginResList.stream()
                    .filter(res -> resType.equals(res.getType()))
                    .collect(Collectors.toList());
        }

        @Override
        public <R extends Res> Collection<R> getSubItems(String resType, String resId, int loadDeep) {

            return null;
        }

    };

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

    @Override
    public String getId() {
        return pid;
    }

    @Override
    public String getName() {
        return "插件" + pid;
    }


} // end class
