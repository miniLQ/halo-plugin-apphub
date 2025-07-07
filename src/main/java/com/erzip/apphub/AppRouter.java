package com.erzip.apphub;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import com.erzip.apphub.extension.Release;
import com.erzip.apphub.service.ApplicationService;
import com.erzip.apphub.service.ReleaseService;
import com.erzip.apphub.vo.ApplicationVo;
import com.erzip.apphub.vo.ReleaseVo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.TemplateNameResolver;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

@Component
@AllArgsConstructor
public class AppRouter {
    private static final String GROUP_PARAM = "group";

    private final ReactiveSettingFetcher settingFetcher;

    private final TemplateNameResolver templateNameResolver;

    private final ApplicationService applicationService;

    private final ReleaseService releaseService;
    @Bean
    RouterFunction<ServerResponse> appTemplateRouter() {
        return route(GET("/apps/{appId}"), this::renderAppPage); // 1. 添加路径变量占位符
    }

    Mono<ServerResponse> renderAppPage(ServerRequest request) {
        String appId = request.pathVariable("appId");
        Mono<ApplicationVo> application = getRelease(appId);

        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "app-detail")
            .flatMap(templateName -> ServerResponse.ok().render(templateName,
                    Map.of("group", application,
                        "title", getAppsTitle()
                        )
            ));
    }

    @Bean
    RouterFunction<ServerResponse> appsTemplateRouter() {
        return route(GET("/apps"), this::renderAppsPage);
    }

    Mono<ServerResponse> renderAppsPage(ServerRequest request) {
        // 或许你需要准备你需要提供给模板的默认数据，非必须
        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(), "apps")
            .flatMap(templateName -> ServerResponse.ok().render(templateName,
                Map.of("groups", applications(),
                    "plugins",pluginGroups(),
                    "themes",themeGroups(),
                    ModelConst.TEMPLATE_ID, "applications",
                    "title", getAppsTitle()
                )
            ));
    }

    @Bean
    RouterFunction<ServerResponse> releaseRouter() {
        return route(GET("/releases").or(GET("/releases/page/{page:\\d+}")),
            handlerFunction()
        );
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> ServerResponse.ok().render("releases",
            Map.of("groups", applications(),
                "releases", releaseList(request),
                ModelConst.TEMPLATE_ID, "releases",
                "title", getAppsTitle()
            )
        );
    }

    //获取release信息
    private Mono<ApplicationVo> getRelease(String appId) {
        return applicationService.groupBy(appId)
            .map(application -> {
                // 创建新的 ApplicationVo，其中 ReleaseSpec 的 url 设为 null
                List<ReleaseVo> updatedReleases = application.releases().stream()
                    .map(releaseVo -> {
                        // 获取原始的 ReleaseSpec
                        Release.ReleaseSpec originalSpec = releaseVo.spec();
                        originalSpec.setUrl(null);
                        // 返回更新后的 ReleaseVo
                        return new ReleaseVo(
                            releaseVo.metadata(),
                            originalSpec
                        );
                    })
                    .collect(Collectors.toList());

                // 返回更新后的 ApplicationVo
                return new ApplicationVo(
                    application.metadata(),
                    application.spec(),
                    application.status(),
                    updatedReleases
                );
            });
    }

    private Mono<UrlContextListResult<ReleaseVo>> releaseList(ServerRequest request) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        String group = groupPathQueryParam(request);
        return this.settingFetcher.get("base")
            .map(item -> item.get("pageSize").asInt(10))
            .defaultIfEmpty(10)
            .flatMap(pageSize -> releaseService.list(pageNum, pageSize, group)
                .map(list -> new UrlContextListResult.Builder<ReleaseVo>()
                    .listResult(list)
                    .nextUrl(appendGroupParam(
                        PageUrlUtils.nextPageUrl(path, totalPage(list)), group)
                    )
                    .prevUrl(appendGroupParam(PageUrlUtils.prevPageUrl(path), group))
                    .build()
                )
            );

    }

    private static String appendGroupParam(String path, String group) {
        return UriComponentsBuilder.fromPath(path)
            .queryParamIfPresent(GROUP_PARAM, Optional.ofNullable(group))
            .build()
            .toString();
    }

    private int pageNumInPathVariable(ServerRequest request) {
        String page = request.pathVariables().get("page");
        return NumberUtils.toInt(page, 1);
    }

    private String groupPathQueryParam(ServerRequest request) {
        return request.queryParam(GROUP_PARAM)
            .filter(StringUtils::isNotBlank)
            .orElse(null);
    }

    Mono<String> getAppsTitle() {
        return this.settingFetcher.get("base").map(
            setting -> setting.get("title").asText("应用舱")).defaultIfEmpty(
            "应用舱");
    }

    private Mono<List<ApplicationVo>> applications() {
        return applicationService.listGroups().collectList();
    }


    private Mono<List<ApplicationVo>> pluginGroups() {
        return applicationService.listAppPlugins().collectList();
    }

    private Mono<List<ApplicationVo>> themeGroups() {
        return applicationService.listAppThemes().collectList();
    }


}
