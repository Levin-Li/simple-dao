package com.levin.commons.dao.service;

import com.levin.commons.plugin.Plugin;
import com.levin.commons.plugin.PluginConfigurer;
import com.levin.commons.plugin.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TestPluginConfiguration implements PluginConfigurer {

    @Override
    public void configPlugin(PluginManager pluginManager) {

        pluginManager.installPlugin(newPlugin(), true);
    }


    @Bean
    Plugin newPlugin() {
        return new TestPlugin() {
        };
    }
}
