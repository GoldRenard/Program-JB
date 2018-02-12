package org.goldrenard.jb.parser.base;

import org.apache.commons.io.FilenameUtils;
import org.goldrenard.jb.model.NamedEntity;
import org.goldrenard.jb.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public abstract class NamedResource<T extends NamedEntity> extends HashMap<String, T> implements ParsedResource<T>, Map<String, T> {

    private static final Logger log = LoggerFactory.getLogger(NamedResource.class);

    private final String resourceExtension;

    protected NamedResource(String resourceExtension) {
        Objects.requireNonNull(resourceExtension, "Resource extension is required");
        this.resourceExtension = resourceExtension;
    }

    @Override
    public int read(String path) {
        int count = 0;
        try {
            File folder = new File(path);
            if (folder.exists()) {
                if (log.isTraceEnabled()) {
                    log.trace("Loading resources files from {}", path);
                }
                for (File file : IOUtils.listFiles(folder)) {
                    if (file.isFile() && file.exists()) {
                        String fileName = file.getName();
                        String extension = FilenameUtils.getExtension(fileName);
                        if (resourceExtension.equalsIgnoreCase(extension)) {
                            String resourceName = FilenameUtils.getBaseName(fileName);
                            if (log.isTraceEnabled()) {
                                log.trace("Read AIML resource {} from {}", resourceName, fileName);
                            }
                            T entry = load(resourceName, file);
                            if (entry instanceof Set) {
                                count += ((Set) entry).size();
                            }
                            if (entry instanceof Map) {
                                count += ((Map) entry).size();
                            }
                            put(entry.getName(), entry);
                        }
                    }
                }
            } else {
                log.warn("{} does not exist.", path);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return count;
    }

    protected abstract T load(String resourceName, File file);

    public void write(Collection<T> resources) {
        for (T resource : resources) {
            try {
                write(resource);
            } catch (Exception e) {
                log.error("Could not write resource {}", resource);
            }
        }
    }
}
