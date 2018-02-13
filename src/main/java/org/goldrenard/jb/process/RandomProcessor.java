package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * implements {@code <random>} tag
 */
public class RandomProcessor extends BaseNodeProcessor {

    public RandomProcessor(AIMLProcessor processor) {
        super(processor, "random");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<>();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals("li")) liList.add(childList.item(i));
        }
        int index = (int) (Math.random() * liList.size());
        if (ps.getChatSession() != null && ps.getChatSession().getBot().getConfiguration().isQaTestMode()) {
            index = 0;
        }
        return evalTagContent(liList.get(index), ps, null);
    }
}
