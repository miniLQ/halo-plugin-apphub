package com.erzip.apphub;

import com.erzip.apphub.service.ApplicationService;
import com.erzip.apphub.vo.DownLoadVo;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;


import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Component
@RequiredArgsConstructor
public class DownLoadEndpoint implements CustomEndpoint {

    private final ApplicationService applicationService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route()
                .GET("/download/{releaseName}", this::releaseDownload,
                        builder -> {
                            builder.operationId("ReleaseDownloads")
                                    .description("Get download URL for a release")
                                    .parameter(parameterBuilder()
                                            .name("releaseName")
                                            .in(ParameterIn.PATH)
                                            .description("Release metadata name")
                                            .implementation(DownLoadVo.class)
                                            .required(true)
                                    )
                                    // 修复1: 使用正确的响应类型 (String 而不是 ListResult)
                                    .response(responseBuilder()
                                            .responseCode("200")
                                            .description("Successful operation")
                                            .implementation(DownLoadVo.class)
                                    )
                                    .build();
                        }
                )
                .GET("/viewcount/{groupName}", this::appViewCount,
                        builder -> {
                            builder.operationId("AppviewCounts")
                                    .description("修改app浏览量")
                                    .parameter(parameterBuilder()
                                            .name("groupName")
                                            .in(ParameterIn.PATH)
                                            .description("App metadata name")
                                            .implementation(Integer.class)
                                            .required(true)
                                    )
                                    .response(responseBuilder()
                                            .responseCode("200")
                                            .description("Successful operation")
                                            .implementation(Integer.class)
                                    )
                                    .build();
                        }
                )
                .build();
    }

    private Mono<ServerResponse> appViewCount(ServerRequest serverRequest) {
        String groupName = serverRequest.pathVariable("groupName");
        if (StringUtils.isBlank(groupName)) {
            // 使用 Mono.error 代替直接抛出异常，保持响应式
            return Mono.error(new ServerWebInputException("App metadata name must not be blank"));
        }
        return applicationService.updateViewCount(groupName)
                .flatMap(count ->
                        ServerResponse.ok().bodyValue(count)
                ).onErrorResume(ServerWebInputException.class,
                        e -> ServerResponse.badRequest().bodyValue(e.getReason()));
    }

    private Mono<ServerResponse> releaseDownload(ServerRequest serverRequest) {
        String releaseName = serverRequest.pathVariable("releaseName");
        if (StringUtils.isBlank(releaseName)) {
            // 使用 Mono.error 代替直接抛出异常，保持响应式
            return Mono.error(new ServerWebInputException("Release metadata name must not be blank"));
        }

        return applicationService.getDownloadUrl(releaseName)
                .flatMap(url -> ServerResponse.ok().bodyValue(url))
                .onErrorResume(ServerWebInputException.class,
                        e -> ServerResponse.badRequest().bodyValue(e.getReason()));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.apphub.erzip.com/v1alpha1");
    }
}