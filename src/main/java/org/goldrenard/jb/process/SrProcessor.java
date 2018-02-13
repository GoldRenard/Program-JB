package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

public class SrProcessor extends BaseNodeProcessor {

    public SrProcessor(AIMLProcessor processor) {
        super(processor, "sr");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return processor.respond(ps.getStarBindings().getInputStars().star(0), ps.getThat(), ps.getTopic(),
                ps.getChatSession(), ps.getSraiCount());
    }
}
