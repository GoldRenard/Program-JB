package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * apply the AIML normalization pre-processor to the evaluated tag content.
 * implements {@code <normalize>} tag.
 */
public class NormalizeProcessor extends BaseNodeProcessor {

    public NormalizeProcessor(AIMLProcessor processor) {
        super(processor, "normalize");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().getBot().getPreProcessor().normalize(result);
    }
}
