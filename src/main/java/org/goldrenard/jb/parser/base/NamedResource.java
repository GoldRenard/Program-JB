package org.goldrenard.jb.parser.base;

import org.apache.commons.io.FilenameUtils;
import org.goldrenard.jb.model.NamedEntity;
import org.goldrenard.jb.parser.MapsResource;
import org.goldrenard.jb.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public abstract class NamedResource<T extends NamedEntity> implements ParsedResource<T>, Map<String, T> {

    private static final Logger log = LoggerFactory.getLogger(MapsResource.class);

    private Map<String, T> data = new HashMap<>();

    private final String resourceExtension;

    protected NamedResource(String resourceExtension) {
        Objects.nonNull(resourceExtension);
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

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public T get(Object key) {
        return data.get(key);
    }

    @Override
    public T put(String key, T value) {
        return data.put(key, value);
    }

    @Override
    public T remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<T> values() {
        return data.values();
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        return data.entrySet();
    }
}
