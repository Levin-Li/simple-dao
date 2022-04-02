package ${modulePackageName};

import static ${modulePackageName}.ModuleOption.*;
import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginManager;
import com.levin.commons.service.domain.ApiResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 *  全局插件控制器
 *
 *  @author Auto gen by simple-dao-codegen ${.now}
 */
@RestController
@RequestMapping("/system/plugin")
@Tag(name = "插件", description = "插件管理")
@Slf4j
public class PluginManagerController {

    @Resource
    PluginManager pluginManager;

    @PostConstruct
    public void init() {

    }

    /**
     * 插件列表
     *
     * @return
     */
    @GetMapping("/list")
    @Operation(tags = "插件", summary = "插件列表", description = "插件列表")
    public ApiResp<List<Plugin>> list() {
        return ApiResp.ok(pluginManager.getInstalledPlugins());
    }

    /**
     * 插件菜单
     *
     * @param pluginId
     * @return
     */
    @GetMapping("/{pluginId}")
    @Operation(tags = "插件",summary = "插件详情", description = "插件详情")
    public ApiResp<Plugin> plugin(@PathVariable String pluginId) {
        return ApiResp.ok(pluginManager.getInstalledPlugin(pluginId));
    }

}
