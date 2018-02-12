package org.goldrenard.jb.parser;

import org.apache.commons.io.FileUtils;
import org.goldrenard.jb.Bot;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.AIMLMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class MapsResource extends NamedResource<AIMLMap> {

    private static final Logger log = LoggerFactory.getLogger(MapsResource.class);

    private final Bot bot;

    public MapsResource(Bot bot) {
        super("txt");
        this.bot = bot;
        put(Constants.map_successor, new AIMLMap(Constants.map_successor, bot));
        put(Constants.map_predecessor, new AIMLMap(Constants.map_predecessor, bot));
        put(Constants.map_singular, new AIMLMap(Constants.map_singular, bot));
        put(Constants.map_plural, new AIMLMap(Constants.map_plural, bot));
    }

    @Override
    protected AIMLMap load(String resourceName, File file) {
        AIMLMap aimlMap = new AIMLMap(resourceName, bot);
        try {
            for (String line : FileUtils.readLines(file, "UTF-8")) {
                String[] splitLine = line.split(":");
                if (log.isDebugEnabled()) {
                    log.debug("AIMLMap line={}", line);
                }
                if (splitLine.length >= 2) {
                    if (line.startsWith(Constants.remote_map_key)) {
                        if (splitLine.length >= 3) {
                            aimlMap.setHost(splitLine[1]);
                            aimlMap.setBotId(splitLine[2]);
                            aimlMap.setExternal(true);
                            log.info("Created external map at [host={}, botId={}]", aimlMap.getHost(), aimlMap.getBotId());
                        }
                    } else {
                        String key = splitLine[0].toUpperCase();
                        String value = splitLine[1];
                        // assume domain element is already normalized for speedier load
                        //key = bot.preProcessor.normalize(key).trim();
                        aimlMap.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Read AIML Set error", e);
        }
        return aimlMap;
    }

    @Override
    public int write(Map<String, AIMLMap> resources) {
        return 0;
    }
}
