package ${modulePackageName};

import com.levin.commons.dao.*;
import com.levin.commons.dao.repository.SimpleDaoRepository;
import com.levin.commons.plugin.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.*;

//Auto gen by simple-dao-codegen ${.now}
//模块插件

@Slf4j
@Component
public class ${camelStyleModuleName}Plugin implements Plugin, PluginManagerAware {

    //dao
    @Autowired
    SimpleDaoRepository simpleDaoRepository;

    @Autowired
    private SimpleDao simpleDao;

    final String pid = ModuleOption.PACKAGE_NAME;

    private PluginManager pluginManager;

    @Override
    public List<DataItem> getDataItems() {
        //@todo
        return Collections.emptyList();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        //@todo
        return Collections.emptyList();
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
       log.info("init...");
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

    @Override
    public String getDescription() {
        return getName();
    }

} // end class
