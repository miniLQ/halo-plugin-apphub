package com.erzip.apphub;

import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class AppHubPlugin extends BasePlugin {
    // private final SchemeManager schemeManager;
    public AppHubPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        // this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
    }

    @Override
    public void delete() {
        System.out.println("插件删除！");
    }
}
