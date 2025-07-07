package com.erzip.apphub;

import com.erzip.apphub.extension.Release;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;

public enum AppSorter {
    DISPLAY_NAME,

    CREATE_TIME;

    static final Function<Release, String> name = release -> release.getMetadata()
        .getName();


    public static Comparator<Release> from(AppSorter sorter,
                                           Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }


    static Comparator<Release> from(AppSorter sorter) {
        if (sorter == null) {
            return createTimeComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<Release, Instant> comparatorFunc
                = release -> release.getMetadata().getCreationTimestamp();
            return Comparator.comparing(comparatorFunc).thenComparing(name);
        }

        if (DISPLAY_NAME.equals(sorter)) {
            Function<Release, String> comparatorFunc = moment -> moment.getSpec()
                .getDisplayName();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }

        throw new IllegalStateException("Unsupported sort value: " + sorter);
    }


    static AppSorter convertFrom(String sort) {
        for (AppSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }


    static Comparator<Release> createTimeComparator() {
        Function<Release, Instant> comparatorFunc = release -> release.getMetadata()
            .getCreationTimestamp();
        return Comparator.comparing(comparatorFunc).thenComparing(name);
    }
}
