package com.erzip.apphub.finders;



import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.ReleaseVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

public interface ApplicationFinder {

    Flux<ReleaseVo> listAll();

    Mono<ListResult<ReleaseVo>> list(Integer page, Integer size);

    Mono<ListResult<ReleaseVo>> list(Integer page, Integer size, String group);

    Flux<ReleaseVo> listBy(String groupName);

    Flux<ApplicationVo> groupBy();


    Mono<ApplicationVo> groupBy(String groupName);

    //查找插件应用
    Flux<ApplicationVo> listAppPlugins();

    //查找主题应用
    Flux<ApplicationVo> listAppThemes();
    Flux<ApplicationVo> listGroups();

    //修改下载量
    Mono<Integer> updateDownloadCount(String name);

    //增加浏览记录
    Mono<Integer> updateViewCount(String name);
}
