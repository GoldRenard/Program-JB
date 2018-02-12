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
package org.goldrenard.jb.utils;

import org.goldrenard.jb.Bot;
import org.goldrenard.jb.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utilities {

    private static final Logger log = LoggerFactory.getLogger(Utilities.class);

    /**
     * Excel sometimes adds mysterious formatting to CSV files.
     * This function tries to clean it up.
     *
     * @param line line from AIMLIF file
     * @return reformatted line
     */
    public static String fixCSV(String line) {
        while (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1);
        }
        if (line.startsWith("\"")) {
            line = line.substring(1, line.length());
        }
        if (line.endsWith("\"")) {
            line = line.substring(0, line.length() - 1);
        }
        return line.replaceAll("\"\"", "\"");
    }

    public static String tagTrim(String xmlExpression, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (xmlExpression.length() >= (stag + etag).length()) {
            xmlExpression = xmlExpression.substring(stag.length());
            xmlExpression = xmlExpression.substring(0, xmlExpression.length() - etag.length());
        }
        return xmlExpression;
    }

    public static Set<String> stringSet(String... strings) {
        return new HashSet<>(Arrays.asList(strings));
    }

    public static String getFileFromInputStream(InputStream in) {
        String strLine;
        StringBuilder contents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.startsWith(Constants.text_comment_mark)) {
                    if (strLine.length() != 0) {
                        contents.append(strLine);
                    }
                    contents.append("\n");
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return contents.toString().trim();
    }

    public static String getFile(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                try (FileInputStream stream = new FileInputStream(filename)) {
                    return getFileFromInputStream(stream);
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return "";
    }

    public static String getCopyrightFromInputStream(InputStream in) {
        StringBuilder copyright = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (strLine.length() != 0) {
                    copyright.append("<!-- ").append(strLine).append(" -->");
                }
                copyright.append("\n");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return copyright.toString();
    }

    public static String getCopyright(Bot bot, String AIMLFilename) {
        String copyright = "";
        String year = CalendarUtils.year();
        String date = CalendarUtils.date();
        try {
            copyright = getFile(bot.getConfigPath() + "/copyright.txt");
            String[] splitCopyright = copyright.split("\n");
            copyright = "";

            StringBuilder builder = new StringBuilder();
            for (String part : splitCopyright) {
                builder.append("<!-- ").append(part).append(" -->\n");
            }
            copyright = builder.toString();
            copyright = copyright.replace("[url]", bot.getProperties().get("url"));
            copyright = copyright.replace("[date]", date);
            copyright = copyright.replace("[YYYY]", year);
            copyright = copyright.replace("[version]", bot.getProperties().get("version"));
            copyright = copyright.replace("[botname]", bot.getName().toUpperCase());
            copyright = copyright.replace("[filename]", AIMLFilename);
            copyright = copyright.replace("[botmaster]", bot.getProperties().get("botmaster"));
            copyright = copyright.replace("[organization]", bot.getProperties().get("organization"));
        } catch (Exception e) {//Catch exception if any
            log.error("Error: ", e);
        }
        return copyright;
    }

    /**
     * Returns if a character is one of Chinese-Japanese-Korean characters.
     *
     * @param c the character to be tested
     * @return true if CJK, false otherwise
     */
    public static boolean isCharCJK(final char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
    }
}
