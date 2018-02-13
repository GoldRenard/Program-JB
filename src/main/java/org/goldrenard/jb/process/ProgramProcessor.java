package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * return a string indicating the name and version of the AIML program.
 * implements {@code <program/>}
 */
public class ProgramProcessor extends BaseNodeProcessor {

    public ProgramProcessor(AIMLProcessor processor) {
        super(processor, "program");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return ps.getChatSession().getBot().getConfiguration().getProgramName();
    }
}
