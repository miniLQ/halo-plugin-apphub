package com.erzip.apphub.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com", version = "v1alpha1", kind = "Application", plural = "applications",
    singular = "application")
public class Application extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private ApplicationSpec spec;

    @Schema
    private PostGroupStatus status;

    @Data
    public static class ApplicationSpec{

        @Schema(requiredMode = REQUIRED,description = "应用类型",example = "PLUGIN")
        private ApplicationType type;

        @Schema(requiredMode = REQUIRED, description = "应用封面", example = "https://example.com/image.png")
        private String cover;

        @Schema(requiredMode = REQUIRED, description = "应用名称", example = "greeting插件")
        private String displayName;

        @Schema(description = "应用描述", example = "个人博客问候语插件")
        private String description;

        @Schema(description = "应用截图")
        private List<AppScreenshot> screenshots;

        @Schema(description = "应用特点")
        private List<AppFeature> features;

        @Schema(description = "应用作者")
        private Owner owner;

        @Schema(description = "价格信息")
        private PriceConfig priceConfig;

        @Schema(description = "是否发布",example = "true")
        private Boolean publish;
        @Schema(description = "是否下架",example = "false")
        private Boolean deprecated;

        private Integer priority;
    }

    @Data
    public static class PriceConfig{
        private ModeType mode;
        @JsonInclude(JsonInclude.Include.ALWAYS) // 确保0L值也被序列化
        private Double oneTimePrice;
    }

    public enum ModeType{
        FREE, ONE_TIME
    }
    public enum ApplicationType {
        PLUGIN, THEME
    }

    @Data
    public static class PostGroupStatus{
        // 发布的版本数量
        public Integer releaseCount;
        @JsonInclude(JsonInclude.Include.ALWAYS) // 确保0L值也被序列化
        @Schema(description = "应用浏览量", example = "0")
        private Integer viewCount;
        @JsonInclude(JsonInclude.Include.ALWAYS) // 确保0L值也被序列化
        @Schema(description = "应用下载量", example = "0")
        private Integer downloadCount;

    }

    @JsonIgnore
    public PostGroupStatus getStatusOrDefault(){
        if (this.status == null){
            this.status = new PostGroupStatus();
        }
        return this.status;
    }



    @Data
    public static class Owner{
        @Schema(description = "发布者名称")
        private String name;
        @Schema(description = "发布者头像")
        private String avatar;
        @Schema(description = "发布者标签")
        private String label;
        @Schema(description = "发布者描述")
        private String description;

        @Schema(description = "发布者主页地址")
        private String homepage;

        @Schema(description = "github仓库地址")
        private String repo;

        @Schema (description = "github issues地址")
        private String issues;

        @Schema(description = "协议")
        private String license;
    }

    @Data
    public static class AppScreenshot{
        private String displayName;
        private String description;
        private String url;
    }

    @Data
    public static class AppFeature{
        private String feature;
    }


}
