package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <input index="N"/>} tag
 */
public class InputProcessor extends BaseNodeProcessor {

    public InputProcessor(AIMLProcessor processor) {
        super(processor, "input");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getInputHistory().getString(index);
    }
}
