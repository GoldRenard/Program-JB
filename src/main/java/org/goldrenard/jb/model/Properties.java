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

import org.goldrenard.jb.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * Bot Properties
 */
public class Properties extends HashMap<String, String> {

    private static final Logger log = LoggerFactory.getLogger(Properties.class);

    /**
     * get the value of a bot property.
     *
     * @param key property name
     * @return property value or a string indicating the property is undefined
     */
    public String get(String key) {
        String result = super.get(key);
        return result != null ? result : Constants.default_property;
    }

    /**
     * Read bot properties from an input stream.
     *
     * @param in Input stream
     */
    public int getPropertiesFromInputStream(InputStream in) {
        int cnt = 0;
        String strLine;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(":")) {
                    String property = strLine.substring(0, strLine.indexOf(":"));
                    String value = strLine.substring(strLine.indexOf(":") + 1);
                    put(property, value);
                    cnt++;
                }
            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return cnt;
    }

    /**
     * Read bot properties from a file.
     *
     * @param filename file containing bot properties
     */
    public int getProperties(String filename) {
        int cnt = 0;
        if (log.isTraceEnabled()) {
            log.trace("Get Properties: {}", filename);
        }
        try {
            // Open the file that is the first
            // command line parameter
            File file = new File(filename);
            if (file.exists()) {
                try (FileInputStream fstream = new FileInputStream(filename)) {
                    cnt = getPropertiesFromInputStream(fstream);
                }
            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return cnt;
    }
}
