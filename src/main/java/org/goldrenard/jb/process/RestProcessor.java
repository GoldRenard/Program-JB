package org.goldrenard.jb.process;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

public class RestProcessor extends BaseNodeProcessor {

    public RestProcessor(AIMLProcessor processor) {
        super(processor, "rest");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String content = evalTagContent(node, ps, null);
        content = ps.getChatSession().getBot().getPreProcessor().normalize(content);
        return restWords(content);
    }

    private static String restWords(String sentence) {
        String content = (sentence == null ? "" : sentence);
        content = content.trim();
        if (content.contains(" ")) {
            return content.substring(content.indexOf(" ") + 1, content.length());
        }
        return Constants.default_list_item;
    }
}
