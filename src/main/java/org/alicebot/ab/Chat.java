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

import lombok.Getter;
import lombok.Setter;
import org.alicebot.ab.configuration.MagicBooleans;
import org.alicebot.ab.configuration.MagicNumbers;
import org.alicebot.ab.configuration.MagicStrings;
import org.alicebot.ab.model.History;
import org.alicebot.ab.model.Predicates;
import org.alicebot.ab.utils.IOUtils;
import org.alicebot.ab.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Class encapsulating a chat session between a bot and a client
 */
@Getter
@Setter
public class Chat {

    private static final Logger log = LoggerFactory.getLogger(Chat.class);

    private final Bot bot;
    private final TripleStore tripleStore;
    private boolean doWrites;
    private String customerId = MagicStrings.default_Customer_id;
    private History<History> thatHistory = new History<>("that");
    private History<String> requestHistory = new History<>("request");
    private History<String> responseHistory = new History<>("response");
    private History<String> inputHistory = new History<>("input");
    private Predicates predicates = new Predicates();

    /**
     * Constructor  (defualt customer ID)
     *
     * @param bot the bot to chat with
     */
    public Chat(Bot bot) {
        this(bot, true, "0");
    }

    public Chat(Bot bot, boolean doWrites) {
        this(bot, doWrites, "0");
    }

    /**
     * Constructor
     *
     * @param bot        bot to chat with
     * @param customerId unique customer identifier
     */
    public Chat(Bot bot, boolean doWrites, String customerId) {
        this.customerId = customerId;
        this.bot = bot;
        this.tripleStore = new TripleStore("anon", bot);
        this.doWrites = doWrites;
        History<String> contextThatHistory = new History<>();
        contextThatHistory.add(MagicStrings.default_that);
        this.thatHistory.add(contextThatHistory);
        addPredicates();
        addTriples();
        this.predicates.put("topic", MagicStrings.default_topic);
        this.predicates.put("jsenabled", MagicStrings.js_enabled);

        if (log.isTraceEnabled()) {
            log.trace("Chat Session Created for bot {}", bot.getName());
        }
    }

    /**
     * Load all predicate defaults
     */
    private void addPredicates() {
        try {
            predicates.getPredicateDefaults(bot.getConfigPath() + "/predicates.txt");
        } catch (Exception e) {
            log.warn("Error reading predicates", e);
        }
    }

    /**
     * Load Triple Store knowledge base
     */
    private int addTriples() {
        int count = 0;
        String fileName = bot.getConfigPath() + "/triples.txt";
        if (log.isTraceEnabled()) {
            log.trace("Loading Triples from {}", fileName);
        }
        File f = new File(fileName);
        if (f.exists()) {
            try (InputStream is = new FileInputStream(f)) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String strLine;
                    //Read File Line By Line
                    while ((strLine = br.readLine()) != null) {
                        String[] triple = strLine.split(":");
                        if (triple.length >= 3) {
                            String subject = triple[0];
                            String predicate = triple[1];
                            String object = triple[2];
                            tripleStore.addTriple(subject, predicate, object);
                            count++;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error reading triples", e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Loaded {} triples", count);
        }
        return count;
    }

    /**
     * Chat session terminal interaction
     */
    public void chat() {
        try {
            String request = "SET PREDICATES";
            String response = multisentenceRespond(request);
            while (!"quit".equals(request)) {
                log.info("Human: ");
                request = IOUtils.readInputTextLine();
                response = multisentenceRespond(request);
                log.info("Robot: {}", response);
            }
        } catch (Exception e) {
            log.warn("Error: ", e);
        }
    }

    /**
     * Return bot response to a single sentence input given conversation context
     *
     * @param input              client input
     * @param that               bot's last sentence
     * @param topic              current topic
     * @param contextThatHistory history of "that" values for this request/response interaction
     * @return bot's reply
     */
    private String respond(String input, String that, String topic, History<String> contextThatHistory) {
        boolean repetition = true;
        for (int i = 0; i < MagicNumbers.repetition_count; i++) {
            if (inputHistory.get(i) == null || !input.toUpperCase().equals(inputHistory.get(i).toUpperCase())) {
                repetition = false;
            }
        }
        if (input.equals(MagicStrings.null_input)) {
            repetition = false;
        }
        inputHistory.add(input);
        if (repetition) {
            input = MagicStrings.repetition_detected;
        }

        String response;

        response = bot.getProcessor().respond(input, that, topic, this);
        String normResponse = bot.getPreProcessor().normalize(response);
        if (MagicBooleans.jp_tokenize) {
            normResponse = JapaneseUtils.tokenizeSentence(normResponse);
        }
        String sentences[] = bot.getPreProcessor().sentenceSplit(normResponse);
        for (String s : sentences) {
            if (s.trim().equals("")) {
                s = MagicStrings.default_that;
            }
            contextThatHistory.add(s);
        }
        return response.trim() + " ";
    }

    /**
     * Return bot response given an input and a history of "that" for the current conversational interaction
     *
     * @param input              client input
     * @param contextThatHistory history of "that" values for this request/response interaction
     * @return bot's reply
     */
    private String respond(String input, History<String> contextThatHistory) {
        History hist = thatHistory.get(0);
        String that = hist != null ? hist.getString(0) : MagicStrings.default_that;
        return respond(input, that, predicates.get("topic"), contextThatHistory);
    }

    /**
     * return a compound response to a multiple-sentence request. "Multiple" means one or more.
     *
     * @param request client's multiple-sentence input
     * @return Response
     */
    public String multisentenceRespond(String request) {
        StringBuilder response = new StringBuilder();
        try {
            String normalized = bot.getPreProcessor().normalize(request);
            if (MagicBooleans.jp_tokenize) {
                normalized = JapaneseUtils.tokenizeSentence(normalized);
            }
            String sentences[] = bot.getPreProcessor().sentenceSplit(normalized);
            History<String> contextThatHistory = new History<>("contextThat");
            for (String sentence : sentences) {
                String reply = respond(sentence, contextThatHistory);
                response.append(" ").append(reply);
            }

            String result = response.toString();
            requestHistory.add(request);
            responseHistory.add(result);
            thatHistory.add(contextThatHistory);
            result = result.replaceAll("[\n]+", "\n");
            result = result.trim();
            if (doWrites) {
                bot.writeLearnfIFCategories();
            }
            return result;
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return MagicStrings.error_bot_response;
    }
}
