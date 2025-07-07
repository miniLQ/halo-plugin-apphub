package com.erzip.apphub.extension;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.erzip.com",version = "v1alpha1", kind = "ApplicationComment", plural = "applicationcomments", singular = "applicationcomment")
public class ApplicationComment extends AbstractExtension {

    @Data
    public class ApplicationCommentSpec{
        private Boolean haloCommentEnabled;
    }
}
