package com.erzip.apphub.vo;

import com.erzip.apphub.extension.Application;
import java.util.List;
import run.halo.app.extension.MetadataOperator;

public record ApplicationVo(MetadataOperator metadata, Application.ApplicationSpec spec,
                            Application.PostGroupStatus status,
                            List<ReleaseVo> releases) {
}
