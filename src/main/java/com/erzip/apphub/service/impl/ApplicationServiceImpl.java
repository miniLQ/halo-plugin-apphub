package com.erzip.apphub.service.impl;

import com.erzip.apphub.extension.Release;
import com.erzip.apphub.extension.Application;
import com.erzip.apphub.service.ApplicationService;
import com.erzip.apphub.service.ReleaseService;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.DownLoadVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.IListRequest.QueryListRequest;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

@Component
public class ApplicationServiceImpl implements ApplicationService {
    private final ReactiveExtensionClient client;
    @Autowired
    private ReleaseService releaseService;

    public ApplicationServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }


    private ListOptions toListOptions(QueryListRequest query) {
        return labelAndFieldSelectorToListOptions(
            query.getLabelSelector(), query.getFieldSelector()
        );
    }

    @Override
    public Mono<ListResult<Application>> listApplication(QueryListRequest query) {
        return this.client.listBy(
                Application.class,
                toListOptions(query),
                PageRequestImpl.of(query.getPage(), query.getSize())
            )
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .flatMap(this::populateReleases)
                .collectList()
                .map(groups -> new ListResult<>(
                    listResult.getPage(),
                    listResult.getSize(),
                    listResult.getTotal(),
                    groups
                ))
            );
    }

    @Override
    public Mono<Application> deleteApplication(String name) {
        return this.client.fetch(Application.class, name)
            .flatMap(this.client::delete)
            .flatMap(deleted -> {
                    var listOptions = ListOptions.builder()
                        .andQuery(QueryFactory.equal("spec.groupName", name))
                        .build();
                    return this.client.listAll(Release.class, listOptions, Sort.unsorted())
                        .flatMap(this.client::delete)
                        .then()
                        .thenReturn(deleted);
                }
            );
    }

    @Override
    public Mono<Integer> updateViewCount(String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.error(new ServerWebInputException("App group name must not be blank"));
        }
        return this.client.fetch(Application.class,name)
            .switchIfEmpty(Mono.error(new ServerWebInputException(
                "App group not found: " + name
            )))
            .flatMap(group ->{
                group.getStatusOrDefault().setViewCount(group.getStatusOrDefault().getViewCount()+1);
                return this.client.update(group)
                    .thenReturn(group.getStatusOrDefault().getViewCount());
            });
    }

    @Override
    public Flux<ApplicationVo> groupBy() {
        return this.client.listAll(Application.class,
                ListOptions.builder().build(), defaultSort())
            .concatMap(group -> {
                return releaseService.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(
                        releases -> {
                            Application.PostGroupStatus status = group.getStatus();
                            status.setReleaseCount(releases.size());
                            return new ApplicationVo(group.getMetadata(), group.getSpec(),status,releases);
                        });
            });
    }

    @Override
    public Mono<ApplicationVo> groupBy(String groupName) {
        return this.client.listAll(Application.class,
                ListOptions.builder().build(), defaultSort())
            .filter(group -> group.getMetadata().getName().equals(groupName))
            .next()
            .flatMap(group ->
                releaseService.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(releases -> {
                        Application.PostGroupStatus status = group.getStatus();
                        status.setReleaseCount(releases.size());
                        return new ApplicationVo(
                            group.getMetadata(),
                            group.getSpec(),
                            status,
                            releases
                        );
                    })
            );
    }

    @Override
    public Flux<ApplicationVo> listAppPlugins() {
        return listAppsByType(Application.ApplicationType.PLUGIN.name());
    }

    @Override
    public Flux<ApplicationVo> listAppThemes() {
        return listAppsByType(Application.ApplicationType.THEME.name());
    }

    @Override
    public Flux<ApplicationVo> listGroups() {
        return this.client.listAll(Application.class,
                ListOptions.builder().build(), defaultSort())
            .concatMap(group -> {
                return releaseService.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(
                        releases -> {
                            Application.PostGroupStatus status = group.getStatus();
                            status.setReleaseCount(releases.size());
                            return new ApplicationVo(group.getMetadata(), group.getSpec(),status,null);
                        });
            });
    }

    private Flux<ApplicationVo> listAppsByType(String type){
        return this.client.listAll(Application.class,
                ListOptions.builder()
                    .fieldQuery(
                        QueryFactory.equal("spec.type",type)
                    )
                    .build(), defaultSort())
            .concatMap(group -> {
                return releaseService.listBy(group.getMetadata().getName())
                    .collectList()
                    .map(
                        releases -> {
                            Application.PostGroupStatus status = group.getStatus();
                            status.setReleaseCount(releases.size());
                            return new ApplicationVo(group.getMetadata(), group.getSpec(),status,null);
                        });
            });
    }

    @Override
    public Mono<Integer> updateDownloadCount(String name) {
        if (StringUtils.isBlank(name)) {
            return Mono.error(new ServerWebInputException("App group name must not be blank"));
        }

        return this.client.fetch(Application.class,name)
            .switchIfEmpty(Mono.error(new ServerWebInputException(
                "App group not found: " + name
            )))
            .flatMap(group ->{
                group.getStatusOrDefault().setDownloadCount(group.getStatusOrDefault().getDownloadCount()+1);
                return this.client.update(group).thenReturn(group.getStatusOrDefault().getDownloadCount());
            });
    }

    @Override
    public Mono<DownLoadVo> getDownloadUrl(String releaseName) {
        return this.client.listAll(Release.class,
                ListOptions.builder()
                    .fieldQuery(QueryFactory.equal("metadata.name", releaseName))
                    .build(), defaultSort())
            .next()
            .switchIfEmpty(Mono.error(new RuntimeException("未找到应用" + releaseName)))
            .flatMap(release -> {
                // 1. 从应用获取分组名称
                String groupName = release.getSpec().getGroupName();

                // 2. 处理付费模式并传递结果
                return this.client.fetch(Application.class, groupName)
                    .switchIfEmpty(Mono.error(new RuntimeException("未找到应用分组: " + groupName)))
                    .flatMap(app -> {
                        boolean isOneTime = app.getSpec().getPriceConfig().getMode().equals(Application.ModeType.ONE_TIME);

                        // 3. 更新下载次数并传递结果
                        return updateDownloadCount(groupName)
                            .thenReturn(Tuples.of(release, isOneTime));  // 传递release和付费模式
                    });
            })
            .flatMap(tuple -> {
                boolean isOneTime = tuple.getT2();
                Release release = tuple.getT1();
                // 3. 处理URL返回逻辑
                Release.ReleaseSpec spec = release.getSpec();
                if (spec == null) {
                    return Mono.error(new IllegalStateException("应用规格为空"));
                }



                // 根据付费模式决定URL值
                String url = isOneTime ? "#" : spec.getUrl();
                if (!isOneTime && (url == null || url.isBlank())) {
                    return Mono.error(new IllegalStateException("应用URL为空"));
                }

                return Mono.just(new DownLoadVo(url));
            });
    }



    private static Sort defaultSort() {
        return Sort.by(
            asc("spec.priority"),
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }

    private Mono<Application> populateReleases(Application application) {
        return fetchReleaseCount(application)
            .doOnNext(count -> application.getStatusOrDefault().setReleaseCount(count))
            .thenReturn(application);
    }

    private Mono<Integer> fetchReleaseCount(Application application) {
        Assert.notNull(application, "应用分组不能为null");
        String name = application.getMetadata().getName();

        return client.list(
                Release.class,
                release -> !release.isDeleted()
                    &&
                    release.getSpec().getGroupName().equals(name),
                null
            )
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }
}
