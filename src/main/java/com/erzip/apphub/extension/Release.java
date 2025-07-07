package com.erzip.apphub.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com",version = "v1alpha1", kind = "Release", plural = "releases", singular = "release")
public class Release extends AbstractExtension {
    private ReleaseSpec spec;


    @Data
    public class ReleaseSpec {

        @Schema(requiredMode = REQUIRED, description = "版本", example = "V1.0.0")
        private String displayName;

        @Schema(requiredMode = REQUIRED, description = "版本号", example = "1.0.0")
        private String version;

        @Schema(requiredMode = REQUIRED, description = "halo版本要求", example = ">=2.21.0")
        private String requires;

        @Schema(requiredMode = REQUIRED, description = "安装包大小")
        private String packageSize;
        @Schema(requiredMode = REQUIRED, description = "下载地址")
        private String url;

        @Schema(requiredMode = REQUIRED, description = "更新描述")
        private List<UpdateSpec> updateSpecs;

        @Schema(description = "发布时间戳", example = "2025-06-17T07:48:38.457152531Z")
        private Instant publishTimestamp;
        private Integer priority;

        @Schema(requiredMode = REQUIRED,pattern = "^\\S+$",description = "分组名称")
        private String groupName;
    }


    @Data
    public static class UpdateSpec{
        @Schema(requiredMode = REQUIRED, description = "更新标题")
        private String title;
        @Schema(requiredMode = REQUIRED, description = "更新内容")
        private List<Note> notes;
    }

    @Data
    public static class Note{
        @Schema(requiredMode = REQUIRED, description = "更新描述")
        private String description;
    }

    @JsonIgnore
    public boolean isDeleted(){
        return Objects.equals(true,getMetadata().getDeletionTimestamp() != null);
    }
}
