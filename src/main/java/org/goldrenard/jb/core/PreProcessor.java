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
package org.goldrenard.jb.core;

import org.goldrenard.jb.model.Substitution;
import org.goldrenard.jb.parser.SubstitutionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;

/**
 * AIML Preprocessor and substitutions
 */
public class PreProcessor {

    private static final Logger log = LoggerFactory.getLogger(PreProcessor.class);

    private final Bot bot;

    private final SubstitutionResource normal;

    private final SubstitutionResource denormal;

    private final SubstitutionResource person;

    private final SubstitutionResource person2;

    private final SubstitutionResource gender;

    /**
     * Constructor given bot
     *
     * @param bot AIML bot
     */
    public PreProcessor(Bot bot) {
        this.bot = bot;
        normal = new SubstitutionResource(bot);
        denormal = new SubstitutionResource(bot);
        person = new SubstitutionResource(bot);
        person2 = new SubstitutionResource(bot);
        gender = new SubstitutionResource(bot);

        normal.read(bot.getConfigPath() + "/normal.txt");
        denormal.read(bot.getConfigPath() + "/denormal.txt");
        person.read(bot.getConfigPath() + "/person.txt");
        person2.read(bot.getConfigPath() + "/person2.txt");
        gender.read(bot.getConfigPath() + "/gender.txt");
        if (log.isTraceEnabled()) {
            log.trace("PreProcessor: {} norms, {} denorms, {} persons, {} person2, {} genders ",
                    normal.size(), denormal.size(), person.size(), person2.size(), gender.size());
        }
    }

    /**
     * Apply a sequence of subsitutions to an input string
     *
     * @param request input request
     * @return result of applying substitutions to input
     */
    private String substitute(SubstitutionResource resource, String request) {
        String result = " " + request + " ";
        try {
            for (Substitution substitution : resource) {
                Matcher matcher = substitution.getPattern().matcher(result);
                if (matcher.find()) {
                    result = matcher.replaceAll(substitution.getSubstitution());
                }
            }
            while (result.contains("  ")) {
                result = result.replace("  ", " ");
            }
            result = result.trim();
        } catch (Exception e) {
            log.error("Request {} Result {}", request, result, e);
        }
        return result.trim();
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
        String result = substitute(normal, request);
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
        return substitute(denormal, request);
    }

    /**
     * personal pronoun substitution for {@code <person></person>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String person(String input) {
        return substitute(person, input);
    }

    /**
     * personal pronoun substitution for {@code <person2></person2>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String person2(String input) {
        return substitute(person2, input);
    }

    /**
     * personal pronoun substitution for {@code <gender>} tag
     *
     * @param input sentence
     * @return sentence with pronouns swapped
     */
    public String gender(String input) {
        return substitute(gender, input);
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
