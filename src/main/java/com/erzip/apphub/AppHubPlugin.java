package com.erzip.apphub;

import com.erzip.apphub.extension.Release;
import com.erzip.apphub.extension.ApplicationComment;
import com.erzip.apphub.extension.Application;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

@Component
public class AppHubPlugin extends BasePlugin {
    private final SchemeManager schemeManager;
    public AppHubPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        System.out.println("插件启动成功！");
        schemeManager.register(Release.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.groupName")
                .setIndexFunc(simpleAttribute(Release.class, release->
                    release.getSpec() == null ? "" : release.getSpec().getGroupName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.displayName")
                .setIndexFunc(simpleAttribute(Release.class, release ->
                    release.getSpec() == null ? "" : release.getSpec().getDisplayName()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Release.class, release ->
                    release.getSpec() == null || release.getSpec().getPriority() == null
                        ? String.valueOf(0) : release.getSpec().getPriority().toString()
                ))
            );
        });
        schemeManager.register(Application.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName("spec.type")
                .setIndexFunc(simpleAttribute(Application.class, group ->
                    group.getSpec() == null ? "PLUGIN" : group.getSpec().getType().toString()
                ))
            );
            indexSpecs.add(new IndexSpec()
                .setName("spec.priority")
                .setIndexFunc(simpleAttribute(Application.class, group ->
                    group.getSpec() == null || group.getSpec().getPriority() == null
                        ? String.valueOf(0) : group.getSpec().getPriority().toString()
                ))
            );
        });
        schemeManager.register(ApplicationComment.class);
    }

    @Override
    public void stop() {
        System.out.println("插件停止！");
        schemeManager.unregister(schemeManager.get(Release.class));
        schemeManager.unregister(schemeManager.get(Application.class));
        schemeManager.unregister(schemeManager.get(ApplicationComment.class));
    }
}
