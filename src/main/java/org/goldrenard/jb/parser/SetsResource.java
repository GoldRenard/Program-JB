package org.goldrenard.jb.parser;

import org.apache.commons.io.FileUtils;
import org.goldrenard.jb.Bot;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.AIMLSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

public class SetsResource extends NamedResource<AIMLSet> {

    private static final Logger log = LoggerFactory.getLogger(SetsResource.class);

    private final Bot bot;

    public SetsResource(Bot bot) {
        super("txt");
        this.bot = bot;
        put(Constants.natural_number_set_name, new AIMLSet(Constants.natural_number_set_name, bot));
    }

    @Override
    protected AIMLSet load(String resourceName, File file) {
        AIMLSet aimlSet = new AIMLSet(resourceName, bot);
        try {
            for (String line : FileUtils.readLines(file, "UTF-8")) {
                // strLine = bot.getPreProcessor().normalize(strLine).toUpperCase();
                // assume the set is pre-normalized for faster loading
                if (line.startsWith("external")) {
                    String[] splitLine = line.split(":");
                    if (splitLine.length >= 4) {
                        aimlSet.setHost(splitLine[1]);
                        aimlSet.setBotId(splitLine[2]);
                        aimlSet.setMaxLength(Integer.parseInt(splitLine[3]));
                        aimlSet.setExternal(true);
                        log.info("Created external set at {} {}", aimlSet.getHost(), aimlSet.getBotId());
                    }
                } else {
                    line = line.toUpperCase().trim();
                    String[] splitLine = line.split(" ");
                    int length = splitLine.length;
                    if (length > aimlSet.getMaxLength()) {
                        aimlSet.setMaxLength(length);
                    }
                    aimlSet.add(line.trim());
                }
            }
        } catch (Exception e) {
            log.error("Read AIML Set error", e);
        }
        return aimlSet;
    }

    @Override
    public int write( Map<String, AIMLSet> resources) {
        return 0;
    }
}
