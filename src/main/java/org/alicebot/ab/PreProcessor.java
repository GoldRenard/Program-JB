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
package org.alicebot.ab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AIML Preprocessor and substitutions
 */
public class PreProcessor {

    private static final Logger log = LoggerFactory.getLogger(PreProcessor.class);

    private int normalCount = 0;
    private int denormalCount = 0;
    private int personCount = 0;
    private int person2Count = 0;
    private int genderCount = 0;
    private String[] normalSubs = new String[MagicNumbers.max_substitutions];
    private Pattern[] normalPatterns = new Pattern[MagicNumbers.max_substitutions];
    private String[] denormalSubs = new String[MagicNumbers.max_substitutions];
    private Pattern[] denormalPatterns = new Pattern[MagicNumbers.max_substitutions];
    private String[] personSubs = new String[MagicNumbers.max_substitutions];
    private Pattern[] personPatterns = new Pattern[MagicNumbers.max_substitutions];
    private String[] person2Subs = new String[MagicNumbers.max_substitutions];
    private Pattern[] person2Patterns = new Pattern[MagicNumbers.max_substitutions];
    private String[] genderSubs = new String[MagicNumbers.max_substitutions];
    private Pattern[] genderPatterns = new Pattern[MagicNumbers.max_substitutions];

    /**
     * Constructor given bot
     *
     * @param bot AIML bot
     */
    public PreProcessor(Bot bot) {
        normalCount = readSubstitutions(bot.getConfigPath() + "/normal.txt", normalPatterns, normalSubs);
        denormalCount = readSubstitutions(bot.getConfigPath() + "/denormal.txt", denormalPatterns, denormalSubs);
        personCount = readSubstitutions(bot.getConfigPath() + "/person.txt", personPatterns, personSubs);
        person2Count = readSubstitutions(bot.getConfigPath() + "/person2.txt", person2Patterns, person2Subs);
        genderCount = readSubstitutions(bot.getConfigPath() + "/gender.txt", genderPatterns, genderSubs);
        if (log.isTraceEnabled()) {
            log.trace("Preprocessor: {} norms {} persons {} person2 ", normalCount, personCount, person2Count);
        }
    }

    /**
     * apply normalization substitutions to a request
     *
     * @param request client input
     * @return normalized client input
     */
    public String normalize(String request) {
        if (log.isDebugEnabled()) {
            log.debug("PreProcessor.normalize(request: {})", request);
        }
        String result = substitute(request, normalPatterns, normalSubs, normalCount);
        result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
        if (log.isDebugEnabled()) {
            log.debug("PreProcessor.normalize() returning: {}", result);
        }
        return result;
    }

    /**
     * apply denormalization substitutions to a request
     *
     * @param request client input
     * @return normalized client input
     */
    public String denormalize(String request) {
        return substitute(request, denormalPatterns, denormalSubs, denormalCount);
    }

    /**
     * personal pronoun substitution for {@code <person></person>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String person(String input) {
        return substitute(input, personPatterns, personSubs, personCount);
    }

    /**
     * personal pronoun substitution for {@code <person2></person2>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String person2(String input) {
        return substitute(input, person2Patterns, person2Subs, person2Count);
    }

    /**
     * personal pronoun substitution for {@code <gender>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String gender(String input) {
        return substitute(input, genderPatterns, genderSubs, genderCount);

    }

    /**
     * Apply a sequence of subsitutions to an input string
     *
     * @param request  input request
     * @param patterns array of patterns to match
     * @param subs     array of substitution values
     * @param count    number of patterns and substitutions
     * @return result of applying substitutions to input
     */
    private String substitute(String request, Pattern[] patterns, String[] subs, int count) {
        String result = " " + request + " ";
        int index = 0;
        try {
            for (int i = 0; i < count; i++) {
                index = i;
                String replacement = subs[i];
                Pattern p = patterns[i];
                Matcher m = p.matcher(result);
                if (m.find()) {
                    result = m.replaceAll(replacement);
                }
            }
            while (result.contains("  ")) {
                result = result.replace("  ", " ");
            }
            result = result.trim();
        } catch (Exception e) {
            log.error("Request {} Result {} at {} {} {}", request, result, index, patterns[index], subs[index], e);
        }
        return result.trim();
    }

    /**
     * read substitutions from input stream
     *
     * @param in       input stream
     * @param patterns array of patterns
     * @param subs     array of substitution values
     * @return number of patterns substitutions read
     */
    public int readSubstitutionsFromInputStream(InputStream in, Pattern[] patterns, String[] subs) {
        int subCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String strLine;
            //Read File Line By Line
            while ((strLine = reader.readLine()) != null) {
                strLine = strLine.trim();
                if (!strLine.startsWith(MagicStrings.text_comment_mark)) {
                    Pattern pattern = Pattern.compile("\"(.*?)\",\"(.*?)\"", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(strLine);
                    if (matcher.find() && subCount < MagicNumbers.max_substitutions) {
                        subs[subCount] = matcher.group(2);
                        String quotedPattern = Pattern.quote(matcher.group(1));
                        patterns[subCount] = Pattern.compile(quotedPattern, Pattern.CASE_INSENSITIVE);
                        subCount++;
                    }
                }

            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return subCount;
    }

    /**
     * read substitutions from a file
     *
     * @param filename name of substitution file
     * @param patterns array of patterns
     * @param subs     array of substitution values
     * @return number of patterns and substitutions read
     */
    private int readSubstitutions(String filename, Pattern[] patterns, String[] subs) {
        int subCount = 0;
        try {
            // Open the file that is the first
            // command line parameter
            File file = new File(filename);
            if (file.exists()) {
                try (FileInputStream stream = new FileInputStream(filename)) {
                    subCount = readSubstitutionsFromInputStream(stream, patterns, subs);
                }
            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return (subCount);
    }

    /**
     * Split an input into an array of sentences based on sentence-splitting characters.
     *
     * @param line input text
     * @return array of sentences
     */
    public String[] sentenceSplit(String line) {
        line = line.replace("。", ".");
        line = line.replace("？", "?");
        line = line.replace("！", "!");
        String result[] = line.split("[\\.!\\?]");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return result;
    }

    /**
     * normalize a file consisting of sentences, one sentence per line.
     *
     * @param infile  input file
     * @param outfile output file to write results
     */
    public void normalizeFile(String infile, String outfile) {
        try (FileInputStream stream = new FileInputStream(infile)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(outfile))) {
                    String strLine;
                    //Read File Line By Line
                    while ((strLine = reader.readLine()) != null) {
                        strLine = strLine.trim();
                        if (strLine.length() > 0) {
                            String norm = normalize(strLine).toUpperCase();
                            String sentences[] = sentenceSplit(norm);
                            if (sentences.length > 1) {
                                for (String s : sentences) {
                                    log.info("{}-->{}", norm, s);
                                }
                            }
                            for (String sentence : sentences) {
                                sentence = sentence.trim();
                                if (sentence.length() > 0) {
                                    writer.write(sentence);
                                    writer.newLine();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
    }
}
