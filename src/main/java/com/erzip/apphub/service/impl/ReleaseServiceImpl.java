package com.erzip.apphub.service.impl;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToListOptions;

import com.erzip.apphub.AppQuery;
import com.erzip.apphub.extension.Application;
import com.erzip.apphub.extension.Release;
import com.erzip.apphub.service.ReleaseService;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.ReleaseVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;

@Component
public class ReleaseServiceImpl implements ReleaseService {

    private final ReactiveExtensionClient client;

    public ReleaseServiceImpl(ReactiveExtensionClient client){
        this.client = client;
    }

    @Override
    public Mono<ListResult<Release>> listRelease(AppQuery query) {
        return this.client.listBy(
            Release.class,
            toListOptions(query),
            PageRequestImpl.of(query.getPage(), query.getSize(), query.getSort()));
    }





    @Override
    public Flux<ReleaseVo> listBy(String groupName) {
        var options = ListOptions.builder()
            .andQuery(QueryFactory.equal("spec.groupName", groupName))
            .build();
        return client.listAll(Release.class, options, defaultSort()).map(ReleaseVo::new);
    }

    @Override
    public Flux<ReleaseVo> listAll() {
        return this.client.listAll(
                Release.class,
                ListOptions.builder().build(),
                defaultSort()
            )
            .map(ReleaseVo::new);
    }

    @Override
    public Mono<ListResult<ReleaseVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }

    @Override
    public Mono<ListResult<ReleaseVo>> list(Integer page, Integer size, String group) {
        return pageRelease(page, size, group);
    }
    private Mono<ListResult<ReleaseVo>> pageRelease(Integer page, Integer size,String group){
        var builder = ListOptions.builder();
        if (StringUtils.isNotEmpty(group)) {
            builder.andQuery(QueryFactory.equal("spec.groupName", group));
        }
        return client.listBy(Release.class, builder.build(),
                PageRequestImpl.of(page, size, defaultSort()))
            .flatMap(listResult -> Flux.fromStream(listResult.get())
                .map(ReleaseVo::new)
                .collectList()
                .map(list -> new ListResult<>(
                    listResult.getPage(), listResult.getSize(), listResult.getTotal(), list
                ))
            );
    }
    private static Sort defaultSort() {
        return Sort.by(
            asc("spec.priority"),
            desc("metadata.creationTimestamp"),
            asc("metadata.name")
        );
    }



    private ListOptions toListOptions(AppQuery query) {
        var builder = ListOptions.builder(labelAndFieldSelectorToListOptions(
            query.getLabelSelector(), query.getFieldSelector())
        );

        if (StringUtils.isNotBlank(query.getKeyword())) {
            builder.andQuery(QueryFactory.contains("spec.displayName", query.getKeyword()));
        }
        if (StringUtils.isNotBlank(query.getGroup())) {
            builder.andQuery(QueryFactory.equal("spec.groupName", query.getGroup()));
        }
        return builder.build();
    }

}
