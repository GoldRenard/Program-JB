package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * evaluate tag contents and return result in lower case
 * implements {@code <lowercase>} tag
 */
public class LowercaseProcessor extends BaseNodeProcessor {

    public LowercaseProcessor(AIMLProcessor processor) {
        super(processor, "lowercase");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toLowerCase();
    }
}
