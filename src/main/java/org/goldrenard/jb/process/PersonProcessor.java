package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * evaluate tag contents and swap 1st and 2nd person pronouns
 * implements {@code <person>} tag
 */
public class PersonProcessor extends BaseNodeProcessor {

    public PersonProcessor(AIMLProcessor processor) {
        super(processor, "person");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result;
        if (node.hasChildNodes()) {
            result = evalTagContent(node, ps, null);
        } else {
            result = ps.getStarBindings().getInputStars().star(0);   // for <person/>
        }
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().person(result);
        return result.trim();
    }
}
