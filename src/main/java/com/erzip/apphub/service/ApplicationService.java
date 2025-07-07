package com.erzip.apphub.service;


import com.erzip.apphub.extension.Application;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.DownLoadVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

public interface ApplicationService {
    Mono<ListResult<Application>> listApplication(QueryListRequest query);

    Mono<Application> deleteApplication(String name);

    //修改下载量
    Mono<Integer> updateDownloadCount(String name);
    //获取下载地址+下载量+1
    Mono<DownLoadVo> getDownloadUrl(String releaseName);

    //修改浏览量
    Mono<Integer> updateViewCount(String name);

    //获取应用详情
    Flux<ApplicationVo> groupBy();
    //根据groupName获取应用详情
    Mono<ApplicationVo> groupBy(String groupName);
    //查找插件应用
    Flux<ApplicationVo> listAppPlugins();
    //查找主题应用
    Flux<ApplicationVo> listAppThemes();

    //查找所有分组
    Flux<ApplicationVo> listGroups();
}
