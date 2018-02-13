package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * evaluate tag contents and swap 1st and 3rd person pronouns
 * implements {@code <person2>} tag
 */
public class Person2Processor extends BaseNodeProcessor {

    public Person2Processor(AIMLProcessor processor) {
        super(processor, "person2");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result;
        if (node.hasChildNodes()) {
            result = evalTagContent(node, ps, null);
        } else {
            result = ps.getStarBindings().getInputStars().star(0);   // for <person2/>
        }
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().person2(result);
        return result.trim();
    }
}
