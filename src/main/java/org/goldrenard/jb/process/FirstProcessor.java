package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

public class FirstProcessor extends BaseNodeProcessor {

    public FirstProcessor(AIMLProcessor processor) {
        super(processor, "first");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String content = evalTagContent(node, ps, null);
        return firstWord(content);
    }
}
