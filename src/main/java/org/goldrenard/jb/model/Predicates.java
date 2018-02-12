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

import org.goldrenard.jb.Bot;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * Manage client predicates
 */
public class Predicates extends HashMap<String, String> {

    private static final Logger log = LoggerFactory.getLogger(Predicates.class);

    private final Bot bot;

    public Predicates(Bot bot) {
        this.bot = bot;
    }

    /**
     * save a predicate value
     *
     * @param key   predicate name
     * @param value predicate value
     * @return predicate value
     */
    public String put(String key, String value) {
        if (bot.getConfiguration().isJpTokenize()) {
            if (key.equals("topic")) {
                value = JapaneseUtils.tokenizeSentence(value);
            }
        }
        if (key.equals("topic") && value.length() == 0) {
            value = Constants.default_get;
        }
        if (value.equals(bot.getConfiguration().getLanguage().getTooMuchRecursion())) {
            value = Constants.default_list_item;
        }
        return super.put(key, value);
    }

    /**
     * get a predicate value
     *
     * @param key predicate name
     * @return predicate value
     */
    public String get(String key) {
        String result = super.get(key);
        return result != null ? result : Constants.default_get;
    }

    /**
     * Read predicate default values from an input stream
     *
     * @param in input stream
     */
    private void getPredicateDefaultsFromInputStream(InputStream in) {
        String strLine;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(":")) {
                    String property = strLine.substring(0, strLine.indexOf(":"));
                    String value = strLine.substring(strLine.indexOf(":") + 1);
                    put(property, value);
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    /**
     * read predicate defaults from a file
     *
     * @param filename name of file
     */
    public void getPredicateDefaults(String filename) {
        try {
            // Open the file that is the first
            // command line parameter
            File file = new File(filename);
            if (file.exists()) {
                try (FileInputStream stream = new FileInputStream(filename)) {
                    getPredicateDefaultsFromInputStream(stream);
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}


