package org.goldrenard.jb.parser;

import org.apache.commons.lang3.StringUtils;
import org.goldrenard.jb.parser.base.CollectionResource;
import org.goldrenard.jb.utils.Utilities;

public class PronounsResource extends CollectionResource<String> {

    private final static String PRONOUNS_FILE = "pronouns.txt";

    @Override
    public int read(String path) {
        Utilities
                .readFileLines(path + "/" + PRONOUNS_FILE)
                .stream()
                .filter(StringUtils::isNotEmpty).forEach(this::add);
        return size();
    }
}
