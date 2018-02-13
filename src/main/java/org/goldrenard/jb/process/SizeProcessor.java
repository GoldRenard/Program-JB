package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * return the size of the robot brain (number of AIML categories loaded).
 * implements {@code <size/>}
 */
public class SizeProcessor extends BaseNodeProcessor {

    public SizeProcessor(AIMLProcessor processor) {
        super(processor, "size");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int size = ps.getChatSession().getBot().getBrain().getCategories().size();
        return String.valueOf(size);
    }
}
