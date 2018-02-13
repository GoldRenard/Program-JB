package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <response index="N"/>} tag
 */
public class ResponseProcessor extends BaseNodeProcessor {

    public ResponseProcessor(AIMLProcessor processor) {
        super(processor, "response");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getResponseHistory().getString(index).trim();
    }
}
