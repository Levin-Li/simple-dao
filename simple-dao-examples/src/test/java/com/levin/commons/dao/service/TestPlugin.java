package com.levin.commons.dao.service;

import com.levin.commons.dao.repository.SimpleDaoRepository;
import com.levin.commons.plugin.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TestPlugin implements Plugin, PluginManagerAware {

    @Autowired
    SimpleDaoRepository simpleDaoRepository;

    final String pid = getClass().getSimpleName() + "-" + System.identityHashCode(this);

    private PluginManager pluginManager;

    @Override
    public List<DataItem> getDataItems() {
        return null;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        return null;
    }

    @Override
    public boolean onEvent(Object... objects) {

        log.debug(getDescription() + " onEvent " + Arrays.asList(objects));

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
        return "测试插件" + pid;
    }

    @Override
    public String getDescription() {
        return getName();
    }

}
