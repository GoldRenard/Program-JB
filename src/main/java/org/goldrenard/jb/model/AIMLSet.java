package org.goldrenard.jb.model;
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

import lombok.Getter;
import org.goldrenard.jb.Bot;
import org.goldrenard.jb.Sraix;
import org.goldrenard.jb.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * implements AIML Sets
 */
public class AIMLSet extends HashSet<String> {

    private static final Logger log = LoggerFactory.getLogger(AIMLSet.class);

    private static final Pattern DIGITS_PATTERN = Pattern.compile("[0-9]+");

    private String setName;

    @Getter
    private int maxLength = 1; // there are no empty sets

    private String host; // for external sets
    private String botId; // for external sets
    private boolean isExternal = false;
    private Bot bot;
    private HashSet<String> inCache = new HashSet<>();
    private HashSet<String> outCache = new HashSet<>();

    /**
     * constructor
     *
     * @param name name of set
     */
    public AIMLSet(String name, Bot bot) {
        super();
        this.bot = bot;
        this.setName = name.toLowerCase();
        if (setName.equals(Constants.natural_number_set_name)) {
            maxLength = 1;
        }
    }

    public boolean contains(String s) {
        if (isExternal && bot.getConfiguration().isEnableExternalSets()) {
            if (inCache.contains(s)) {
                return true;
            }
            if (outCache.contains(s)) {
                return false;
            }
            String[] split = s.split(" ");
            if (split.length > maxLength) {
                return false;
            }
            String query = Constants.set_member_string + setName.toUpperCase() + " " + s;
            String response = Sraix.sraix(null, bot, query, "false", null, host, botId, null, "0");
            if ("true".equals(response)) {
                inCache.add(s);
                return true;
            } else {
                outCache.add(s);
                return false;
            }
        } else if (setName.equals(Constants.natural_number_set_name)) {
            return DIGITS_PATTERN.matcher(s).matches();
        }
        return super.contains(s);
    }

    public void writeAIMLSet() {
        log.info("Writing AIML Set {}", setName);
        try (FileWriter stream = new FileWriter(bot.getSetsPath() + "/" + setName + ".txt")) {
            try (BufferedWriter out = new BufferedWriter(stream)) {
                for (String p : this) {
                    out.write(p.trim());
                    out.newLine();
                }
            }
        } catch (Exception e) {
            log.error("Write error", e);
        }
    }

    private int readAIMLSetFromInputStream(InputStream in, Bot bot) {
        String strLine;
        int cnt = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            while ((strLine = reader.readLine()) != null && strLine.length() > 0) {
                cnt++;
                //strLine = bot.preProcessor.normalize(strLine).toUpperCase();
                // assume the set is pre-normalized for faster loading
                if (strLine.startsWith("external")) {
                    String[] splitLine = strLine.split(":");
                    if (splitLine.length >= 4) {
                        host = splitLine[1];
                        botId = splitLine[2];
                        maxLength = Integer.parseInt(splitLine[3]);
                        isExternal = true;
                        log.info("Created external set at {} {}", host, botId);
                    }
                } else {
                    strLine = strLine.toUpperCase().trim();
                    String[] splitLine = strLine.split(" ");
                    int length = splitLine.length;
                    if (length > maxLength) {
                        maxLength = length;
                    }
                    add(strLine.trim());
                }
            }
        } catch (Exception e) {
            log.error("Read error", e);
        }
        return cnt;
    }

    public int readAIMLSet(Bot bot) {
        int count = 0;
        String fileName = bot.getSetsPath() + "/" + setName + ".txt";
        if (log.isTraceEnabled()) {
            log.trace("Reading AIML Set {}", fileName);
        }
        try {
            // Open the file that is the first
            // command line parameter
            File file = new File(fileName);
            if (file.exists()) {
                try (FileInputStream fstream = new FileInputStream(fileName)) {
                    count = readAIMLSetFromInputStream(fstream, bot);
                }
            } else {
                log.warn("{} not found", fileName);
            }
        } catch (Exception e) {
            log.error("Read error", e);
        }
        return count;
    }
}
