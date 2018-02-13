package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <gender>} tag
 * swaps gender pronouns
 */
public class GenderProcessor extends BaseNodeProcessor {

    public GenderProcessor(AIMLProcessor processor) {
        super(processor, "gender");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().gender(result);
        return result.trim();
    }
}
