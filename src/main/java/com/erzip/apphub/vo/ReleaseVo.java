package com.erzip.apphub.vo;

import com.erzip.apphub.extension.Release;
import run.halo.app.extension.MetadataOperator;

public record ReleaseVo(MetadataOperator metadata, Release.ReleaseSpec spec, Release.ReleaseStatus status) {
    public ReleaseVo(Release release){
        this(release.getMetadata(), release.getSpec(),release.getStatusOrDefault());
    }
}
