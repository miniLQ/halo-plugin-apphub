package com.erzip.apphub;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import com.erzip.apphub.extension.Release;
import com.erzip.apphub.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;

@Component
@RequiredArgsConstructor
public class ReleaseEndpoint implements CustomEndpoint {

    private final ReleaseService releaseService;


    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "console.api.apphub.erzip.com/v1alpha1/Release";
        return route()
            .GET("releases", this::listRelease,
                builder -> {
                    builder.operationId("ListReleases")
                        .description("List releases.")
                        .tag(tag)
                        .response(responseBuilder().implementation(
                            ListResult.generateGenericClass(Release.class)));

                    AppQuery.buildParameters(builder);
                }
            )
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.apphub.erzip.com/v1alpha1");
    }

    private Mono<ServerResponse> listRelease(ServerRequest serverRequest) {
        AppQuery query = new AppQuery(serverRequest.exchange());
        return releaseService.listRelease(query)
            .flatMap(releases -> ServerResponse.ok().bodyValue(releases));
    }
}
