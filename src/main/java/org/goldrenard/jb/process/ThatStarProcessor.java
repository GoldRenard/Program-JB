package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <thatstar index="N"/>}
 * returns the value of input words matching the Nth wildcard (or AIML Set) in <that></that>.
 */
public class ThatStarProcessor extends BaseNodeProcessor {

    public ThatStarProcessor(AIMLProcessor processor) {
        super(processor, "thatstar");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getThatStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getThatStars().star(index).trim();
    }
}
