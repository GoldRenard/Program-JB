/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.parser;

import org.apache.commons.io.FileUtils;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.model.AIMLSet;
import org.goldrenard.jb.parser.base.NamedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetsResource extends NamedResource<AIMLSet> {

    private static final String SETS_EXTENSION = "txt";

    private static final Logger log = LoggerFactory.getLogger(SetsResource.class);

    private final Bot bot;

    public SetsResource(Bot bot) {
        super(SETS_EXTENSION);
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
                if (line.startsWith(Constants.remote_set_key)) {
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
    public void write(AIMLSet resource) {
        log.info("Writing AIML Set {}", resource.getName());

        List<String> lines = resource.isExternal()
                ? Collections.singletonList(String.format("external:%s:%s:%s",
                resource.getHost(), resource.getBotId(), resource.getMaxLength()))
                : resource.stream().map(String::trim).collect(Collectors.toList());

        String fileName = bot.getSetsPath() + "/" + resource.getName() + "." + SETS_EXTENSION;

        try {
            FileUtils.writeLines(new File(fileName), "UTF-8", lines);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}
