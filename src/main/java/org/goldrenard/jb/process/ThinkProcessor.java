package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <think>} tag
 * <p>
 * Evaluate the tag contents but return a blank.
 * "Think but don't speak."
 */
public class ThinkProcessor extends BaseNodeProcessor {

    public ThinkProcessor(AIMLProcessor processor) {
        super(processor, "think");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        evalTagContent(node, ps, null);
        return "";
    }
}
