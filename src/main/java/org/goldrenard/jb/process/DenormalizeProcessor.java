package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * apply the AIML denormalization pre-processor to the evaluated tag contenst.
 * implements {@code <denormalize>} tag.
 */
public class DenormalizeProcessor extends BaseNodeProcessor {

    public DenormalizeProcessor(AIMLProcessor processor) {
        super(processor, "denormalize");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().getBot().getPreProcessor().denormalize(result);
    }
}
