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
package org.alicebot.ab.model;

import lombok.Getter;
import lombok.Setter;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.configuration.Constants;
import org.alicebot.ab.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * structure representing an AIML category and operations on Category
 */
@Setter
@Getter
public class Category {

    private static final Logger log = LoggerFactory.getLogger(Category.class);

    private static AtomicLong categoryCnt = new AtomicLong();

    private Bot bot;
    private String pattern;
    private String that;
    private String topic;
    private String template;
    private String filename;
    private int activationCnt;
    private long categoryNumber; // for loading order
    private AIMLSet matches;
    private String validationMessage = "";

    /**
     * Return a set of inputs matching the category
     *
     * @return and AIML Set of elements matching this category
     */
    public AIMLSet getMatches(Bot bot) {
        if (matches != null) {
            return matches;
        }
        return new AIMLSet("No Matches", bot);
    }

    /**
     * get category pattern
     *
     * @return pattern
     */
    public String getPattern() {
        return pattern == null ? "*" : pattern;
    }

    /**
     * get category that pattern
     *
     * @return that pattern
     */
    public String getThat() {
        return that == null ? "*" : that;
    }

    /**
     * get category topic pattern
     *
     * @return topic pattern
     */
    public String getTopic() {
        return topic == null ? "*" : topic;
    }

    /**
     * get category template
     *
     * @return template
     */
    public String getTemplate() {
        return template == null ? "" : template;
    }

    /**
     * get name of AIML file for this category
     *
     * @return file name
     */
    public String getFilename() {
        return filename == null ? Constants.unknownAimlFile : filename;
    }

    /**
     * increment the category activation count
     */
    public void incrementActivationCnt() {
        activationCnt++;
    }

    /**
     * return a string represeting the full pattern path as "{@code input pattern <THAT> that pattern <TOPIC> topic pattern}"
     */
    public String inputThatTopic() {
        return Graphmaster.inputThatTopic(pattern, that, topic);
    }

    /**
     * add a matching input to the matching input set
     *
     * @param input matching input
     */
    public void addMatch(String input, Bot bot) {
        if (matches == null) {
            String setName = this.inputThatTopic()
                    .replace("*", "STAR")
                    .replace("_", "UNDERSCORE")
                    .replace(" ", "-")
                    .replace("<THAT>", "THAT")
                    .replace("<TOPIC>", "TOPIC");
            matches = new AIMLSet(setName, bot);
        }
        matches.add(input);
    }

    /**
     * convert a template to a single-line representation by replacing "," with #Comma and newline with #Newline
     *
     * @return template on a single line of text
     */
    public String getTemplateLine() {
        String result = template;
        result = result.replaceAll("(\r\n|\n\r|\r|\n)", "\\#Newline");
        result = result.replaceAll(
                bot.getConfiguration().getAimlifSplitChar(),
                bot.getConfiguration().getAimlifSplitCharName());
        return result;
    }

    /**
     * restore a template to its original form by replacing #Comma with "," and #Newline with newline.
     *
     * @param line template on a single line of text
     * @return original multi-line template
     */
    private static String lineToTemplate(Bot bot, String line) {
        String result = line.replaceAll("\\#Newline", "\n");
        result = result.replaceAll(
                bot.getConfiguration().getAimlifSplitCharName(),
                bot.getConfiguration().getAimlifSplitChar());
        return result;
    }

    /**
     * convert a category from AIMLIF format to a Category object
     *
     * @param IF Category in AIMLIF format
     * @return Category object
     */
    public static Category IFToCategory(Bot bot, String IF) {
        String[] split = IF.split(bot.getConfiguration().getAimlifSplitChar());
        return new Category(bot, Integer.parseInt(split[0]), split[1], split[2], split[3], lineToTemplate(bot, split[4]), split[5]);
    }

    /**
     * convert a Category object to AIMLIF format
     *
     * @param category Category object
     * @return category in AIML format
     */
    public static String categoryToIF(Category category) {
        String c = category.bot.getConfiguration().getAimlifSplitChar();
        return category.getActivationCnt() + c + category.getPattern() + c + category.getThat() + c +
                category.getTopic() + c + category.getTemplateLine() + c + category.getFilename();
    }

    /**
     * convert a Category object to AIML syntax
     *
     * @param category Category object
     * @return AIML Category
     */
    public static String categoryToAIML(Category category) {
        String topicStart = "";
        String topicEnd = "";
        String thatStatement = "";
        String result = "";
        String pattern = category.getPattern();
        if (pattern.contains("<SET>") || pattern.contains("<BOT")) {
            String[] splitPattern = pattern.split(" ");
            StringBuilder rpattern = new StringBuilder();
            for (String w : splitPattern) {
                if (w.startsWith("<SET>") || w.startsWith("<BOT") || w.startsWith("NAME=")) {
                    w = w.toLowerCase();
                }
                rpattern.append(" ").append(w);
            }
            pattern = rpattern.toString().trim();
        }

        String NL = "\n";
        try {
            if (!category.getTopic().equals("*")) {
                topicStart = "<topic name=\"" + category.getTopic() + "\">" + NL;
                topicEnd = "</topic>" + NL;
            }
            if (!category.getThat().equals("*")) {
                thatStatement = "<that>" + category.getThat() + "</that>";
            }
            result = topicStart + "<category><pattern>" + pattern + "</pattern>" + thatStatement + NL +
                    "<template>" + category.getTemplate() + "</template>" + NL +
                    "</category>" + topicEnd;
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return result;
    }

    /**
     * check to see if a pattern expression is valid in AIML 2.0
     *
     * @param pattern pattern expression
     * @return true or false
     */
    public boolean validPatternForm(String pattern) {
        if (pattern.length() < 1) {
            validationMessage += "Zero length. ";
            return false;
        }
        /*String[] words = pattern.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!(word.matches("[\\p{Hiragana}\\p{Katakana}\\p{Han}\\p{Latin}]*+") || word.equals("*") || word.equals("_"))) {
                System.out.println("Invalid pattern word "+word);
                return false;
            }
        }*/
        return true;
    }

    /**
     * check for valid Category format
     *
     * @return true or false
     */
    public boolean validate() {
        validationMessage = "";
        if (!validPatternForm(pattern)) {
            validationMessage += "Badly formatted <pattern>";
            return false;
        }
        if (!validPatternForm(that)) {
            validationMessage += "Badly formatted <that>";
            return false;
        }
        if (!validPatternForm(topic)) {
            validationMessage += "Badly formatted <topic>";
            return false;
        }
        if (!AIMLProcessor.validTemplate(template)) {
            validationMessage += "Badly formatted <template>";
            return false;
        }
        if (!filename.endsWith(".aiml")) {
            validationMessage += "Filename suffix should be .aiml";
            return false;
        }
        return true;
    }

    /**
     * Constructor
     *
     * @param activationCnt category activation count
     * @param pattern       input pattern
     * @param that          that pattern
     * @param topic         topic pattern
     * @param template      AIML template
     * @param filename      AIML file name
     */

    public Category(Bot bot, int activationCnt, String pattern, String that, String topic, String template, String filename) {
        this.bot = bot;
        if (bot != null && bot.getConfiguration().isFixExcelCsv()) {
            pattern = Utilities.fixCSV(pattern);
            that = Utilities.fixCSV(that);
            topic = Utilities.fixCSV(topic);
            template = Utilities.fixCSV(template);
            filename = Utilities.fixCSV(filename);
        }
        this.pattern = pattern.trim().toUpperCase();
        this.that = that.trim().toUpperCase();
        this.topic = topic.trim().toUpperCase();
        this.template = template.replace("& ", " and "); // XML parser treats & badly
        this.filename = filename;
        this.activationCnt = activationCnt;
        matches = null;
        this.categoryNumber = categoryCnt.incrementAndGet();
    }

    /**
     * Constructor
     *
     * @param activationCnt    category activation count
     * @param patternThatTopic string representing Pattern Path
     * @param template         AIML template
     * @param filename         AIML category
     */
    public Category(Bot bot, int activationCnt, String patternThatTopic, String template, String filename) {
        this(bot, activationCnt,
                patternThatTopic.substring(0, patternThatTopic.indexOf("<THAT>")),
                patternThatTopic.substring(patternThatTopic.indexOf("<THAT>") + "<THAT>".length(), patternThatTopic.indexOf("<TOPIC>")),
                patternThatTopic.substring(patternThatTopic.indexOf("<TOPIC>") + "<TOPIC>".length(), patternThatTopic.length()), template, filename);
    }

    /**
     * compare two categories for sorting purposes based on activation count
     */
    public static Comparator<Category> ACTIVATION_COMPARATOR = (c1, c2) -> c2.getActivationCnt() - c1.getActivationCnt();

    /**
     * compare two categories for sorting purposes based on alphabetical order of patterns
     */
    public static Comparator<Category> PATTERN_COMPARATOR = (c1, c2) -> String.CASE_INSENSITIVE_ORDER.compare(c1.inputThatTopic(), c2.inputThatTopic());

    /**
     * compare two categories for sorting purposes based on category index number
     */
    public static Comparator<Category> CATEGORY_NUMBER_COMPARATOR = Comparator.comparingLong(Category::getCategoryNumber);
}
