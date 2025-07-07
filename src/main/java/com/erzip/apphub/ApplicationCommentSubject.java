package com.erzip.apphub;

import com.erzip.apphub.extension.ApplicationComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.content.comment.CommentSubject;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

@Component
@RequiredArgsConstructor
public class ApplicationCommentSubject implements CommentSubject<ApplicationComment> {
    private final ReactiveExtensionClient client;
    private static final GroupVersionKind APPLICATION_COMMENT_GVK =
        GroupVersionKind.fromExtension(ApplicationComment.class);

    private static final SubjectDisplay FIXED_SUBJECT_DISPLAY =
        new SubjectDisplay("应用舱", "/applications", "应用舱");

    @Override
    public Mono<ApplicationComment> get(String name) {
        return client.get(ApplicationComment.class, name);
    }

    @Override
    public Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return Mono.just(FIXED_SUBJECT_DISPLAY);
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        GroupVersionKind groupVersionKind = new GroupVersionKind(ref.getGroup(),
            ref.getVersion(), ref.getKind()
        );
        return APPLICATION_COMMENT_GVK.equals(groupVersionKind)
            && "plugin-application-comment".equals(ref.getName());
    }
}