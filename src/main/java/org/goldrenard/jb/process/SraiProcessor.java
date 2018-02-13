package org.goldrenard.jb.process;

import org.goldrenard.jb.Bot;
import org.goldrenard.jb.model.Nodemapper;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * implements AIML <srai> tag
 **/
public class SraiProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(SraiProcessor.class);

    public SraiProcessor(AIMLProcessor processor) {
        super(processor, "srai");
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
            response = processor.evalTemplate(leaf.getCategory().getTemplate(), new ParseState(ps.getDepth() + 1, ps.getChatSession(), ps.getInput(), ps.getThat(), topic, leaf, sraiCount));
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
