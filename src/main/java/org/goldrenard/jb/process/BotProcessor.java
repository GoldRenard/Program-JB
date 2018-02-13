package org.goldrenard.jb.process;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * return the value of a bot property.
 * implements {{{@code <bot name="property"/>}}}
 */
public class BotProcessor extends BaseNodeProcessor {

    public BotProcessor(AIMLProcessor processor) {
        super(processor, "bot");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = Constants.default_property;
        String propertyName = getAttributeOrTagValue(node, ps, "name");
        if (propertyName != null) {
            result = ps.getChatSession().getBot().getProperties().get(propertyName).trim();
        }
        return result;
    }
}
