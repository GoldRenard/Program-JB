package org.goldrenard.jb.process;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.History;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * implements the (template-side) {@code <that index="M,N"/>}    tag.
 * returns a normalized sentence.
 */
public class ThatProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThatProcessor.class);

    public ThatProcessor(AIMLProcessor processor) {
        super(processor, "that");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = 0;
        int jndex = 0;
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                String[] spair = value.split(",");
                index = Integer.parseInt(spair[0]) - 1;
                jndex = Integer.parseInt(spair[1]) - 1;
                log.debug("That index={},{}", index, jndex);
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        String that = Constants.unknown_history_item;
        History hist = ps.getChatSession().getThatHistory().get(index);
        if (hist != null) {
            that = (String) hist.get(jndex);
        }
        return that.trim();
    }
}
