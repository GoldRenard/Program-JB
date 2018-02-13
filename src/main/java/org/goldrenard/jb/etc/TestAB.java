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
package org.goldrenard.jb.etc;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.core.Chat;
import org.goldrenard.jb.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by User on 5/13/2014.
 */
public class TestAB {

    private static final Logger log = LoggerFactory.getLogger(TestAB.class);

    public static String sample_file = "sample.random.txt";

    public static void testChat(Bot bot, boolean doWrites) {
        Chat chatSession = new Chat(bot, doWrites);
        bot.getBrain().nodeStats();
        String textLine = "";
        while (true) {
            textLine = IOUtils.readInputTextLine("Human");
            if (textLine == null || textLine.length() < 1) {
                textLine = Constants.null_input;
            }
            switch (textLine) {
                case "q":
                    System.exit(0);
                    break;
                case "wq":
                    bot.writeQuit();
                    System.exit(0);
                    break;
                case "ab":
                    testAB(bot, sample_file);
                    break;
                default:
                    if (log.isTraceEnabled()) {
                        log.trace("STATE={}:THAT={}:TOPIC={}", textLine, chatSession.getThatHistory().get(0).get(0),
                                chatSession.getPredicates().get("topic"));
                    }
                    String response = chatSession.multisentenceRespond(textLine);
                    while (response.contains("&lt;")) {
                        response = response.replace("&lt;", "<");
                    }
                    while (response.contains("&gt;")) {
                        response = response.replace("&gt;", ">");
                    }
                    IOUtils.writeOutputTextLine("Robot", response);
                    break;
            }
        }
    }

    public static void runTests(Bot bot) {
        bot.getConfiguration().setQaTestMode(true);
        Chat chatSession = new Chat(bot, false);
        //        bot.preProcessor.normalizeFile("c:/ab/bots/super/aiml/thats.txt", "c:/ab/bots/super/aiml/normalthats.txt");
        bot.getBrain().nodeStats();
        IOUtils testInput = new IOUtils(bot.getRootPath() + "/data/lognormal-500.txt", "read");
        IOUtils testOutput = new IOUtils(bot.getRootPath() + "/data/lognormal-500-out.txt", "write");
        String textLine = testInput.readLine();
        while (textLine != null) {
            if (textLine.length() < 1) {
                textLine = Constants.null_input;
            }
            if (textLine.equals("q")) {
                System.exit(0);
            } else if (textLine.equals("wq")) {
                bot.writeQuit();
                System.exit(0);
            } else if (textLine.equals("ab")) {
                testAB(bot, sample_file);
            } else if (textLine.equals(Constants.null_input)) {
                testOutput.writeLine("");
            } else if (textLine.startsWith("#")) {
                testOutput.writeLine(textLine);
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("STATE={}:THAT={}:TOPIC={}", textLine, chatSession.getThatHistory().get(0).get(0),
                            chatSession.getPredicates().get("topic"));
                }
                String response = chatSession.multisentenceRespond(textLine);
                while (response.contains("&lt;")) {
                    response = response.replace("&lt;", "<");
                }
                while (response.contains("&gt;")) {
                    response = response.replace("&gt;", ">");
                }
                testOutput.writeLine("Robot: " + response);
            }
            textLine = testInput.readLine();
        }
        testInput.close();
        testOutput.close();
    }

    public static void testAB(Bot bot, String sampleFile) {
        AB ab = new AB(bot, sampleFile);
        ab.ab();
        log.info("Begin Pattern Suggestor Terminal Interaction");
        ab.terminalInteraction();
    }
}
