package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements <topicstar/> and <topicstar index="N"/>
 * returns the value of input words matching the Nth wildcard (or AIML Set) in a topic pattern.
 */
public class TopicStarProcessor extends BaseNodeProcessor {

    public TopicStarProcessor(AIMLProcessor processor) {
        super(processor, "topicstar");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getTopicStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getTopicStars().star(index).trim();
    }
}
