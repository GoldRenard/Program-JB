package org.goldrenard.jb.parser.base;

import org.goldrenard.jb.model.NamedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public interface ParsedResource<T extends NamedEntity> {

    Logger log = LoggerFactory.getLogger(ParsedResource.class);

    int read(String path);

    void write(T resource);

    default void write(Map<String, T> resourceMap) {
        for (T resource : resourceMap.values()) {
            try {
                write(resource);
            } catch (Exception e) {
                log.error("Could not write resource {} {}", resource.getClass(), resource.getName());
            }
        }
    }
}
