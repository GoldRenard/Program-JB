package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * evaluate tag contents and capitalize the first word.
 * implements {@code <sentence>} tag
 */
public class SentenceProcessor extends BaseNodeProcessor {

    public SentenceProcessor(AIMLProcessor processor) {
        super(processor, "sentence");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        if (result.length() > 1) {
            return result.substring(0, 1).toUpperCase() + result.substring(1, result.length());
        }
        return "";
    }
}
