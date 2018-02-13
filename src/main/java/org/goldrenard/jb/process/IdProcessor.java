package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * return the client ID.
 * implements {@code <id/>}
 */
public class IdProcessor extends BaseNodeProcessor {

    public IdProcessor(AIMLProcessor processor) {
        super(processor, "id");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return ps.getChatSession().getCustomerId();
    }
}
