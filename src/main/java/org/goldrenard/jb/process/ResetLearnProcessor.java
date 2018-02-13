package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class ResetLearnProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(ResetLearnProcessor.class);

    public ResetLearnProcessor(AIMLProcessor processor) {
        super(processor, "resetlearnf", "resetlearn");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        try {
            switch (node.getNodeName()) {
                case "resetlearnf":
                    return resetlearnf(ps);
                case "resetlearn":
                    return resetlearn(ps);
                default:
                    throw new IllegalStateException("Unsupported tag");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }

    private static String resetlearnf(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnfCategories();
        return "Deleted Learnf Categories";
    }

    private static String resetlearn(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnCategories();
        return "Deleted Learn Categories";
    }
}
