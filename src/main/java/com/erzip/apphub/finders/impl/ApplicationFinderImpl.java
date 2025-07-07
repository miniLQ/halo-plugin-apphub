package com.erzip.apphub.finders.impl;
import com.erzip.apphub.finders.ApplicationFinder;

import com.erzip.apphub.service.ApplicationService;
import com.erzip.apphub.service.ReleaseService;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.ReleaseVo;

import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import run.halo.app.extension.ListResult;

import run.halo.app.extension.ReactiveExtensionClient;

import run.halo.app.theme.finders.Finder;

@Finder("applicationFinder")
public class ApplicationFinderImpl implements ApplicationFinder {

    private final ReactiveExtensionClient client;
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ReleaseService releaseService;

    public ApplicationFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public Mono<Integer> updateDownloadCount(String name) {
        return applicationService.updateDownloadCount(name);
    }

    //增加浏览记录
    @Override
    public Mono<Integer> updateViewCount(String name) {
        return applicationService.updateViewCount(name);
    }


    //查找所有主题应用，并设置分组信息
    @Override
    public Flux<ApplicationVo> listAppThemes() {
        return applicationService.listAppThemes();
    }

    //查找所有插件应用，并设置分组信息
    @Override
    public Flux<ApplicationVo> listAppPlugins() {
        return applicationService.listAppPlugins();
    }

    //查找所有分组，并设置子项为null
    @Override
    public Flux<ApplicationVo> listGroups() {
        return applicationService.listGroups();
    }

    @Override
    public Flux<ReleaseVo> listAll() {
        return releaseService.listAll();
    }

    @Override
    public Mono<ListResult<ReleaseVo>> list(Integer page, Integer size) {
        return releaseService.list(page, size, null);
    }

    @Override
    public Mono<ListResult<ReleaseVo>> list(Integer page, Integer size, String group) {
        return releaseService.list(page,size,group);
    }

    @Override
    public Flux<ReleaseVo> listBy(String groupName) {
        return releaseService.listBy(groupName);
    }

    @Override
    public Flux<ApplicationVo> groupBy() {
        return applicationService.groupBy();
    }

    @Override
    public Mono<ApplicationVo> groupBy(String groupName) {
        return applicationService.groupBy(groupName);
    }
}
