package com.erzip.apphub;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.Nullable;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

public class AppQuery extends SortableRequest {

    public AppQuery(ServerWebExchange exchange) {
        super(exchange);
    }

    @Schema(description = "按分组查询")
    public String getGroup(){
        return queryParams.getFirst("group");
    }

    @Nullable
    @Schema(description = "按关键字查询")
    public String getKeyword(){
        return queryParams.getFirst("keyword");
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("keyword")
                .description("按关键字筛选的应用")
                .implementation(String.class)
                .required(false))
            .parameter(parameterBuilder()
                .in(ParameterIn.QUERY)
                .name("group")
                .description("应用分组名称")
                .implementation(String.class)
                .required(false))
        ;
    }

}
