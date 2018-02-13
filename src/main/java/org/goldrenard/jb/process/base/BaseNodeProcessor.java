package org.goldrenard.jb.process.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.utils.DomUtils;
import org.goldrenard.jb.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Set;

@RequiredArgsConstructor
public abstract class BaseNodeProcessor implements AIMLNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(BaseNodeProcessor.class);

    protected final AIMLProcessor processor;

    @Getter
    protected final Set<String> tags;

    protected BaseNodeProcessor(AIMLProcessor processor, String... tags) {
        this.processor = processor;
        this.tags = Utilities.stringSet(tags);
    }

    /**
     * in AIML 2.0, an attribute value can be specified by either an XML attribute value
     * or a subtag of the same name.  This function tries to read the value from the XML attribute first,
     * then tries to look for the subtag.
     *
     * @param node          current parse node.
     * @param ps            current parse state.
     * @param attributeName the name of the attribute.
     * @return the attribute value.
     */
    // value can be specified by either attribute or tag
    protected String getAttributeOrTagValue(Node node, ParseState ps, String attributeName) {
        if (log.isTraceEnabled()) {
            log.trace("BaseNodeProcessor.getAttributeOrTagValue (node: {}, attributeName: {})", node, attributeName);
        }
        String result;
        Node m = node.getAttributes().getNamedItem(attributeName);
        if (m == null) {
            NodeList childList = node.getChildNodes();
            result = null;         // no attribute or tag named attributeName
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (log.isTraceEnabled()) {
                    log.trace("getAttributeOrTagValue child = {}", child.getNodeName());
                }
                if (child.getNodeName().equals(attributeName)) {
                    result = evalTagContent(child, ps, null);
                    if (log.isTraceEnabled()) {
                        log.trace("getAttributeOrTagValue result from child = {}", result);
                    }
                }
            }
        } else {
            result = m.getNodeValue();
        }
        if (log.isTraceEnabled()) {
            log.trace("in BaseNodeProcessor.getAttributeOrTagValue (), returning: {}", result);
        }
        return result;
    }

    /**
     * evaluate the contents of an AIML tag.
     * calls recursEval on child tags.
     *
     * @param node             the current parse node.
     * @param ps               the current parse state.
     * @param ignoreAttributes tag names to ignore when evaluating the tag.
     * @return the result of evaluating the tag contents.
     */
    protected String evalTagContent(Node node, ParseState ps, Set<String> ignoreAttributes) {
        if (log.isTraceEnabled()) {
            log.trace("BaseNodeProcessor.evalTagContent(node: {}, ps: {}, ignoreAttributes: {}", node, ps, ignoreAttributes);
            log.trace("in BaseNodeProcessor.evalTagContent, node string: {}", DomUtils.nodeToString(node));
        }
        StringBuilder result = new StringBuilder();
        try {
            NodeList childList = node.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (log.isTraceEnabled()) {
                    log.trace("in BaseNodeProcessor.evalTagContent(), child: {}", child);
                }
                if (ignoreAttributes == null || !ignoreAttributes.contains(child.getNodeName())) {
                    result.append(processor.recursEval(child, ps));
                }
                if (log.isTraceEnabled()) {
                    log.trace("in BaseNodeProcessor.evalTagContent(), result: ", result);
                }
            }
        } catch (Exception e) {
            log.error("Something went wrong with evalTagContent", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("BaseNodeProcessor.evalTagContent() returning: {}", result);
        }
        return result.toString();
    }

    /**
     * get the value of an index attribute and return it as an integer.
     * if it is not recognized as an integer, return 0
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the the integer intex value
     */
    protected int getIndexValue(Node node, ParseState ps) {
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                return Integer.parseInt(value) - 1;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return 0;
    }

    protected static String firstWord(String sentence) {
        String content = (sentence == null ? "" : sentence);
        content = content.trim();
        if (content.contains(" ")) {
            return content.substring(0, content.indexOf(" "));
        } else if (content.length() > 0) {
            return content;
        }
        return Constants.default_list_item;
    }
}
