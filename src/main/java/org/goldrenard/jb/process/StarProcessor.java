package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <star index="N"/>}
 * returns the value of input words matching the Nth wildcard (or AIML Set).
 */
public class StarProcessor extends BaseNodeProcessor {

    public StarProcessor(AIMLProcessor processor) {
        super(processor, "star");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getInputStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getInputStars().star(index).trim();
    }
}
