package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * return the size of the robot vocabulary (number of words the bot can recognize).
 * implements {@code <vocabulary/>}
 */
public class VocabularyProcessor extends BaseNodeProcessor {

    public VocabularyProcessor(AIMLProcessor processor) {
        super(processor, "vocabulary");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int size = ps.getChatSession().getBot().getBrain().getVocabulary().size();
        return String.valueOf(size);
    }
}
