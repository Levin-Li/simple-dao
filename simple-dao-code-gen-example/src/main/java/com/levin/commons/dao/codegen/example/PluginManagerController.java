package com.levin.commons.dao.codegen.example;


import com.levin.commons.plugin.MenuItem;
import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.service.domain.ApiResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;


@RestController
@RequestMapping("/plugin")
@Tag(name = "Plugin", description = "插件管理")
@Slf4j
public class PluginManagerController {

    @Autowired
    PluginManager pluginManager;


    @PostConstruct
    public void init() {

    }

    /**
     * 插件列表
     *
     * @return
     */
    @RequestMapping("")
    @Operation(summary = "查询插件列表", description = "Plugin")
    public ApiResp<List<Plugin>> list() {
        return ApiResp.ok(pluginManager.getInstalledPlugins());
    }

    /**
     * 插件菜单
     *
     * @param pluginId
     * @return
     */
    @RequestMapping("/{pluginId}/menu")
    @Operation(summary = "查询插件菜单", description = "Plugin")
    public ApiResp<List<MenuItem>> menu(@PathVariable String pluginId) {
        return ApiResp.ok(pluginManager.getInstalledPlugin(pluginId).getMenuItems());
    }

}
