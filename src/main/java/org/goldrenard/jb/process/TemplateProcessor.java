package org.goldrenard.jb.process;

import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * implements {@code <template>} tag
 */
public class TemplateProcessor extends BaseNodeProcessor {

    public TemplateProcessor(AIMLProcessor processor) {
        super(processor, "template");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        return evalTagContent(node, ps, null);
    }
}
