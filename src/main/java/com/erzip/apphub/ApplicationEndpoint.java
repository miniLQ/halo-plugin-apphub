package com.erzip.apphub;


import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.erzip.apphub.extension.Application;
import com.erzip.apphub.service.ApplicationService;
import com.erzip.apphub.vo.ApplicationVo;
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
import run.halo.app.extension.ListResult;

@Component
@RequiredArgsConstructor
public class ApplicationEndpoint implements CustomEndpoint {
    private final ApplicationService applicationService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return route()
            .GET("applications", this::listApplication,
                builder -> {
                    builder.operationId("ListApps")
                        .description("List apps.")
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(Application.class))
                        );

                    AppQuery.buildParameters(builder);
                }
            )
            .GET("application/{name}",this::getApplication,
                builder -> {
                    builder.operationId("Apps")
                        .description("apps.")
                        .response(responseBuilder().implementation(
                            Application.class)
                        );
                    AppQuery.buildParameters(builder);
                })
            .DELETE("applications/{name}", this::deleteApplication,
                builder -> builder.operationId("DeleteApplication")
                    .description("Delete app group.")
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .description("App group name")
                        .implementation(String.class)
                        .required(true)
                    )
                    .response(responseBuilder().implementation(Application.class))
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.apphub.erzip.com/v1alpha1");
    }


    private Mono<ServerResponse> getApplication(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        if (StringUtils.isBlank(name)) {
            return Mono.error(new ServerWebInputException("App group name must not be blank."));
        }
        return applicationService.groupBy(name)
            .flatMap(applicationVo ->
                {
                    ApplicationVo application =
                        new ApplicationVo(applicationVo.metadata(), applicationVo.spec(),
                            applicationVo.status(),null);
                    return ServerResponse.ok().bodyValue(application);
                }
                );
    }

    private Mono<ServerResponse> deleteApplication(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        if (StringUtils.isBlank(name)) {
            return Mono.error(new ServerWebInputException("App group name must not be blank."));
        }
        return applicationService.deleteApplication(name)
            .flatMap(application -> ServerResponse.ok().bodyValue(application));
    }

    private Mono<ServerResponse> listApplication(ServerRequest serverRequest) {
        var request = new AppQuery(serverRequest.exchange());
        return applicationService.listApplication(request)
            .flatMap(applications -> ServerResponse.ok().bodyValue(applications));
    }

}
