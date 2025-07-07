package com.erzip.apphub.service;

import com.erzip.apphub.AppQuery;
import com.erzip.apphub.extension.Release;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.ReleaseVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface ReleaseService {
    Mono<ListResult<Release>> listRelease(AppQuery query);

    //根据groupName获取该组下所有应用的最新版本信息
    Flux<ReleaseVo> listBy(String groupName);

    //获取所有应用的最新版本信息
    Flux<ReleaseVo> listAll();

    //分页获取应用的最新版本信息
    Mono<ListResult<ReleaseVo>> list(Integer page, Integer size);
    //分页获取应用的最新版本信息
    Mono<ListResult<ReleaseVo>> list(Integer page, Integer size, String group);
}
