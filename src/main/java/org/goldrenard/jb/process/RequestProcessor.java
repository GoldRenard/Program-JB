package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <request index="N"/>} tag
 */
public class RequestProcessor extends BaseNodeProcessor {

    public RequestProcessor(AIMLProcessor processor) {
        super(processor, "request");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getRequestHistory().getString(index).trim();
    }
}
