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

import org.goldrenard.jb.configuration.BotConfiguration;
import org.goldrenard.jb.core.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        BotConfiguration.BotConfigurationBuilder builder = BotConfiguration
                .builder()
                .name("alice2")
                .action("chat")
                .jpTokenize(false)
                .graphShortCuts(true);

        for (String s : args) {
            String[] splitArg = s.split("=");
            if (splitArg.length >= 2) {
                String option = splitArg[0];
                String value = splitArg[1];
                if (option.equals("bot")) {
                    builder.name(value);
                }
                if (option.equals("action")) {
                    builder.action(value);
                }
                if (option.equals("morph")) {
                    builder.jpTokenize(value.equals("true"));
                }
            }
        }
        BotConfiguration configuration = builder.build();
        log.info(configuration.getProgramName());
        Bot bot = new Bot(configuration);

        if (bot.getBrain().getCategories().size() < AB.brain_print_size) {
            bot.getBrain().printGraph();
        }
        log.debug("Action = '{}'", configuration.getAction());

        switch (configuration.getAction()) {
            case "chat":
            case "chat-app":
                boolean doWrites = !configuration.getAction().equals("chat-app");
                TestAB.testChat(bot, doWrites);
                break;
            case "ab":
                TestAB.testAB(bot, TestAB.sample_file);
                break;
            case "aiml2csv":
                bot.writeAIMLIFFiles();
                break;
            case "csv2aiml":
                bot.writeAIMLFiles();
                break;
            case "abwq":
                AB ab = new AB(bot, TestAB.sample_file);
                ab.abwq();
                break;
            case "test":
                TestAB.runTests(bot);
                break;
            case "shadow":
                bot.shadowChecker();
                break;
            default:
                log.error("Unrecognized action {}", configuration.getAction());
                break;
        }
    }
}
