package org.goldrenard.jb.parser.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public interface ParsedResource<T> {

    Logger log = LoggerFactory.getLogger(ParsedResource.class);

    int read(String path);

    default void write(T resource) {
        // no default implementation
    }

    default void write(Collection<T> resources) {
        for (T resource : resources) {
            try {
                write(resource);
            } catch (Exception e) {
                log.error("Could not write resource {}", resource);
            }
        }
    }
}
