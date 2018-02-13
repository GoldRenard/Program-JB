package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * evaluate tag contents and return result in upper case
 * implements {@code <uppercase>} tag
 */
public class UppercaseProcessor extends BaseNodeProcessor {

    public UppercaseProcessor(AIMLProcessor processor) {
        super(processor, "uppercase");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toUpperCase();
    }
}
