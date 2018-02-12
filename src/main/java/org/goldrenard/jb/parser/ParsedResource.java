package org.goldrenard.jb.parser;

import org.goldrenard.jb.model.NamedEntity;

import java.util.Map;

public interface ParsedResource<T extends NamedEntity> {

    int read(String path);

    int write(Map<String, T> resourceMap);
}
