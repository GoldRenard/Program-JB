/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/
package org.goldrenard.jb.model;

import lombok.Getter;
import lombok.Setter;
import org.goldrenard.jb.Bot;
import org.goldrenard.jb.Sraix;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.i18n.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * implements AIML Map
 * <p>
 * A map is a function from one string set to another.
 * Elements of the domain are called keys and elements of the range are called values.
 */
@Getter
@Setter
public class AIMLMap extends HashMap<String, String> implements NamedEntity {

    private static final Logger log = LoggerFactory.getLogger(AIMLMap.class);

    private String mapName;
    private String host;    // for external maps
    private String botId;   // for external maps
    private boolean isExternal = false;
    private Bot bot;

    /**
     * constructor to create a new AIML Map
     *
     * @param name the name of the map
     */
    public AIMLMap(String name, Bot bot) {
        super();
        this.bot = bot;
        this.mapName = name;
    }

    /**
     * return a map value given a key
     *
     * @param key the domain element
     * @return the range element or a string indicating the key was not found
     */
    public String get(String key) {
        String value;
        if (mapName.equals(Constants.map_successor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number + 1);
            } catch (Exception e) {
                return Constants.default_map;
            }
        } else if (mapName.equals(Constants.map_predecessor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number - 1);
            } catch (Exception e) {
                return Constants.default_map;
            }
        } else if (mapName.equals("singular")) {
            return Inflector.getInstance().singularize(key).toLowerCase();
        } else if (mapName.equals("plural")) {
            return Inflector.getInstance().pluralize(key).toLowerCase();
        } else if (isExternal && bot.getConfiguration().isEnableExternalMaps()) {
            //String[] split = key.split(" ");
            String query = mapName.toUpperCase() + " " + key;
            String response = Sraix.sraix(null, bot, query, Constants.default_map, null, host, botId, null, "0");
            log.info("External {}({})={}" + response, mapName, key, response);
            value = response;
        } else {
            value = super.get(key);
        }
        if (value == null) {
            value = Constants.default_map;
        }
        if (log.isDebugEnabled()) {
            log.debug("AIMLMap get {}={}", key, value);
        }
        return value;
    }

    /**
     * put a new key, value pair into the map.
     *
     * @param key   the domain element
     * @param value the range element
     * @return the value
     */
    public String put(String key, String value) {
        if (log.isDebugEnabled()) {
            log.debug("AIMLMap put {}={}", key, value);
        }
        return super.put(key, value);
    }

    public void writeAIMLMap() {
        log.info("Writing AIML Map {}", mapName);
        try (FileWriter stream = new FileWriter(bot.getMapsPath() + "/" + mapName + ".txt")) {
            try (BufferedWriter out = new BufferedWriter(stream)) {
                for (String p : this.keySet()) {
                    p = p.trim();
                    if (log.isDebugEnabled()) {
                        log.debug("{}-->{}", p, this.get(p));
                    }
                    out.write(p + ":" + this.get(p).trim());
                    out.newLine();
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    @Override
    public String getName() {
        return mapName;
    }
}
