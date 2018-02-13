package org.goldrenard.jb.process;

import org.goldrenard.jb.Sraix;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.Utilities;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * access external web service for response
 * implements <sraix></sraix>
 * and its attribute variations.
 */
public class SraixProcessor extends BaseNodeProcessor {

    public SraixProcessor(AIMLProcessor processor) {
        super(processor, "sraix");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        Set<String> attributeNames = Utilities.stringSet("botid", "host");
        String host = getAttributeOrTagValue(node, ps, "host");
        String botid = getAttributeOrTagValue(node, ps, "botid");
        String hint = getAttributeOrTagValue(node, ps, "hint");
        String limit = getAttributeOrTagValue(node, ps, "limit");
        String defaultResponse = getAttributeOrTagValue(node, ps, "default");
        String evalResult = evalTagContent(node, ps, attributeNames);
        return Sraix.sraix(ps.getChatSession(), ps.getChatSession().getBot(),
                evalResult, defaultResponse, hint, host, botid, null, limit);
    }
}
