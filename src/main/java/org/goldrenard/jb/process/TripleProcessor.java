package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class TripleProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(TripleProcessor.class);

    public TripleProcessor(AIMLProcessor processor) {
        super(processor, "addtriple", "deletetriple");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        try {
            switch (node.getNodeName()) {
                case "addtriple":
                    return addTriple(node, ps);
                case "deletetriple":
                    return deleteTriple(node, ps);
                default:
                    throw new IllegalStateException("Unsupported tag");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }

    private String deleteTriple(Node node, ParseState ps) {
        String subject = getAttributeOrTagValue(node, ps, "subj");
        String predicate = getAttributeOrTagValue(node, ps, "pred");
        String object = getAttributeOrTagValue(node, ps, "obj");
        return ps.getChatSession().getTripleStore().deleteTriple(subject, predicate, object);
    }

    private String addTriple(Node node, ParseState ps) {
        String subject = getAttributeOrTagValue(node, ps, "subj");
        String predicate = getAttributeOrTagValue(node, ps, "pred");
        String object = getAttributeOrTagValue(node, ps, "obj");
        return ps.getChatSession().getTripleStore().addTriple(subject, predicate, object);
    }
}
