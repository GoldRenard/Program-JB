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
package org.goldrenard.jb.tags;

import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.model.Nodemapper;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.goldrenard.jb.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * implements AIML <srai> tag
 **/
public class SraiProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(SraiProcessor.class);

    public SraiProcessor() {
        super("srai");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        Bot bot = ps.getChatSession().getBot();
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.srai(node: {}, ps: {}", node, ps);
        }
        int sraiCount = ps.getSraiCount() + 1;
        if (sraiCount > bot.getConfiguration().getMaxRecursionCount()
                || ps.getDepth() > bot.getConfiguration().getMaxRecursionDepth()) {
            return bot.getConfiguration().getLanguage().getTooMuchRecursion();
        }
        String response = bot.getConfiguration().getLanguage().getDefaultResponse();
        try {
            String result = evalTagContent(node, ps, null);
            result = result.trim();
            result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
            result = ps.getChatSession().getBot().getPreProcessor().normalize(result);
            if (bot.getConfiguration().isJpTokenize()) {
                result = JapaneseUtils.tokenizeSentence(result);
            }
            String topic = ps.getChatSession().getPredicates().get("topic");     // the that stays the same, but the topic may have changed
            if (log.isTraceEnabled()) {
                log.trace("<srai>{}</srai> from {} topic={}", result, ps.getLeaf().getCategory().inputThatTopic(), topic);
            }
            Nodemapper leaf = ps.getChatSession().getBot().getBrain().match(result, ps.getThat(), topic);
            if (leaf == null) {
                return response;
            }
            if (log.isTraceEnabled()) {
                log.trace("Srai returned {}:{}, that=", leaf.getCategory().inputThatTopic(), leaf.getCategory().getTemplate());
            }
            response = ps.getProcessor().evalTemplate(leaf.getCategory().getTemplate(), new ParseState(ps.getProcessor(),
                    ps.getDepth() + 1, ps.getChatSession(), ps.getInput(), ps.getThat(), topic, leaf, sraiCount));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        String result = response.trim();
        if (log.isTraceEnabled()) {
            log.trace("in SraiProcessor.srai(), returning: {}", result);
        }
        return result;
    }
}
