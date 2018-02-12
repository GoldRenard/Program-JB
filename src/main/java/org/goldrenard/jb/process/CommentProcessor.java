package org.goldrenard.jb.process;

import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

public class CommentProcessor extends BaseNodeProcessor {

    public CommentProcessor(AIMLProcessor processor) {
        super(processor, "#comment");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return "";
    }
}
