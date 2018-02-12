package org.goldrenard.jb.process;

import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

public class TextProcessor extends BaseNodeProcessor {

    public TextProcessor(AIMLProcessor processor) {
        super(processor, "#text");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return node.getNodeValue();
    }
}
