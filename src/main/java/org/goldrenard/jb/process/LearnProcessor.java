package org.goldrenard.jb.process;

import org.goldrenard.jb.Bot;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.Category;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LearnProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(LearnProcessor.class);

    public LearnProcessor(AIMLProcessor processor) {
        super(processor, "learn", "learnf");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        String pattern = "";
        String that = "*";
        String template = "";
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals("category")) {
                NodeList grandChildList = childList.item(i).getChildNodes();
                for (int j = 0; j < grandChildList.getLength(); j++) {
                    if (grandChildList.item(j).getNodeName().equals("pattern")) {
                        pattern = recursLearn(grandChildList.item(j), ps);
                    } else if (grandChildList.item(j).getNodeName().equals("that")) {
                        that = recursLearn(grandChildList.item(j), ps);
                    } else if (grandChildList.item(j).getNodeName().equals("template")) {
                        template = recursLearn(grandChildList.item(j), ps);
                    }
                }
                pattern = pattern.substring("<pattern>".length(), pattern.length() - "</pattern>".length());
                if (log.isTraceEnabled()) {
                    log.trace("Learn Pattern = {}", pattern);
                }
                if (template.length() >= "<template></template>".length())
                    template = template.substring("<template>".length(), template.length() - "</template>".length());
                if (that.length() >= "<that></that>".length())
                    that = that.substring("<that>".length(), that.length() - "</that>".length());
                pattern = pattern.toUpperCase();
                pattern = pattern.replaceAll("\n", " ");
                pattern = pattern.replaceAll("[ ]+", " ");
                that = that.toUpperCase();
                that = that.replaceAll("\n", " ");
                that = that.replaceAll("[ ]+", " ");
                if (log.isTraceEnabled()) {
                    log.trace("Learn Pattern = {}", pattern);
                    log.trace("Learn That = {}", that);
                    log.trace("Learn Template = {}", template);
                }
                Bot bot = ps.getChatSession().getBot();
                Category c;
                if (node.getNodeName().equals("learn")) {
                    c = new Category(bot, 0, pattern, that, "*", template, Constants.nullAimlFile);
                    bot.getLearnGraph().addCategory(c);
                } else {// learnf
                    c = new Category(bot, 0, pattern, that, "*", template, Constants.learnfAimlFile);
                    bot.getLearnfGraph().addCategory(c);
                }
                ps.getChatSession().getBot().getBrain().addCategory(c);
            }
        }
        return "";
    }

    private String unevaluatedAIML(Node node, ParseState ps) {
        String result = learnEvalTagContent(node, ps);
        return AIMLProcessor.unevaluatedXML(result, node, ps);
    }

    private String recursLearn(Node node, ParseState ps) {
        String nodeName = node.getNodeName();
        if ("#text".equals(nodeName)) {
            return node.getNodeValue();
        } else if ("eval".equals(nodeName)) {
            return evalTagContent(node, ps, null);                // AIML 2.0
        }
        return unevaluatedAIML(node, ps);
    }

    private String learnEvalTagContent(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            result.append(recursLearn(child, ps));
        }
        return result.toString();
    }
}
