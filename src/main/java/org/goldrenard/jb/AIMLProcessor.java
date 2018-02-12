/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/
package org.goldrenard.jb;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.*;
import org.goldrenard.jb.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The core AIML parser and interpreter.
 * Implements the AIML 2.0 specification as described in
 * AIML 2.0 Working Draft document
 * https://docs.google.com/document/d/1wNT25hJRyupcG51aO89UcQEiG-HkXRXusukADpFnDs4/pub
 */
public class AIMLProcessor {

    private static final Logger log = LoggerFactory.getLogger(AIMLProcessor.class);

    private Set<AIMLProcessorExtension> extensions = new HashSet<>();

    private int sraiCount = 0;

    private Map<String, Tuple> tupleMap = new ConcurrentHashMap<>();

    private final Bot bot;

    public AIMLProcessor(Bot bot) {
        this.bot = bot;
    }

    /**
     * when parsing an AIML file, process a category element.
     *
     * @param n          current XML parse node.
     * @param categories list of categories found so far.
     * @param topic      value of topic in case this category is wrapped in a <topic> tag
     * @param aimlFile   name of AIML file being parsed.
     */
    private void categoryProcessor(Node n, ArrayList<Category> categories, String topic, String aimlFile, String language) {
        String pattern, that, template;

        NodeList children = n.getChildNodes();
        pattern = "*";
        that = "*";
        template = "";
        for (int j = 0; j < children.getLength(); j++) {
            if (log.isDebugEnabled()) {
                log.debug("CHILD: {}", children.item(j).getNodeName());
            }
            Node m = children.item(j);
            String mName = m.getNodeName();

            switch (mName) {
                case "#text":
                    break; // skip
                case "pattern":
                    pattern = DomUtils.nodeToString(m);
                    break;
                case "that":
                    that = DomUtils.nodeToString(m);
                    break;
                case "topic":
                    topic = DomUtils.nodeToString(m);
                    break;
                case "template":
                    template = DomUtils.nodeToString(m);
                    break;
                default:
                    log.warn("categoryProcessor: unexpected {} in {}", mName, DomUtils.nodeToString(m));
                    break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("categoryProcessor: pattern={}", pattern);
        }
        pattern = trimTag(pattern, "pattern");
        that = trimTag(that, "that");
        topic = trimTag(topic, "topic");
        pattern = cleanPattern(pattern);
        that = cleanPattern(that);
        topic = cleanPattern(topic);

        template = trimTag(template, "template");
        if (bot.getConfiguration().isJpTokenize()) {
            pattern = JapaneseUtils.tokenizeSentence(pattern);
            that = JapaneseUtils.tokenizeSentence(that);
            topic = JapaneseUtils.tokenizeSentence(topic);
        }
        Category c = new Category(bot, 0, pattern, that, topic, template, aimlFile);
        if (StringUtils.isEmpty(template)) {
            log.info("Category {} discarded due to blank or missing <template>.", c.inputThatTopic());
        } else {
            categories.add(c);
        }
    }

    private static String cleanPattern(String pattern) {
        pattern = pattern.replaceAll("(\r\n|\n\r|\r|\n)", " ");
        pattern = pattern.replaceAll("  ", " ");
        return pattern.trim();
    }

    public static String trimTag(String s, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (s.startsWith(stag) && s.endsWith(etag)) {
            s = s.substring(stag.length());
            s = s.substring(0, s.length() - etag.length());
        }
        return s.trim();
    }

    /**
     * convert an AIML file to a list of categories.
     *
     * @param directory directory containing the AIML file.
     * @param aimlFile  AIML file name.
     * @return list of categories.
     */
    public ArrayList<Category> AIMLToCategories(String directory, String aimlFile) {
        try {
            ArrayList<Category> categories = new ArrayList<>();
            Node root = DomUtils.parseFile(directory + "/" + aimlFile);      // <aiml> tag
            String language = bot.getConfiguration().getDefaultLanguage();
            if (root.hasAttributes()) {
                NamedNodeMap XMLAttributes = root.getAttributes();
                for (int i = 0; i < XMLAttributes.getLength(); i++) {
                    if ("language".equals(XMLAttributes.item(i).getNodeName())) {
                        language = XMLAttributes.item(i).getNodeValue();
                    }
                }
            }
            NodeList nodelist = root.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node n = nodelist.item(i);
                if (log.isTraceEnabled()) {
                    log.trace("AIML child: {}", n.getNodeName());
                }
                if ("category".equals(n.getNodeName())) {
                    categoryProcessor(n, categories, "*", aimlFile, language);
                } else if ("topic".equals(n.getNodeName())) {
                    String topic = n.getAttributes().getNamedItem("name").getTextContent();
                    if (log.isTraceEnabled()) {
                        log.trace("topic: {}", topic);
                    }
                    NodeList children = n.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node m = children.item(j);
                        if (log.isTraceEnabled()) {
                            log.trace("Topic child: {}", m.getNodeName());
                        }
                        if ("category".equals(m.getNodeName())) {
                            categoryProcessor(m, categories, topic, aimlFile, language);
                        }
                    }
                }
            }
            return categories;
        } catch (Exception e) {
            log.error("AIMLToCategories Error:", e);
            return null;
        }
    }

    /**
     * generate a bot response to a single sentence input.
     *
     * @param input       the input sentence.
     * @param that        the bot's last sentence.
     * @param topic       current topic.
     * @param chatSession current client session.
     * @return bot's response.
     */
    public String respond(String input, String that, String topic, Chat chatSession) {
        return respond(input, that, topic, chatSession, 0);
    }

    /**
     * generate a bot response to a single sentence input.
     *
     * @param input       input statement.
     * @param that        bot's last reply.
     * @param topic       current topic.
     * @param chatSession current client chat session.
     * @param srCnt       number of <srai> activations.
     * @return bot's reply.
     */
    public String respond(String input, String that, String topic, Chat chatSession, int srCnt) {
        if (log.isTraceEnabled()) {
            log.trace("input: {}, that: {}, topic: {}, chatSession: {}, srCnt: {}", input, that, topic, chatSession, srCnt);
        }
        String response;
        if (input == null || input.length() == 0) {
            input = Constants.null_input;
        }
        sraiCount = srCnt;
        response = chatSession.getBot().getConfiguration().getLanguage().getDefaultResponse();
        try {
            Nodemapper leaf = chatSession.getBot().getBrain().match(input, that, topic);
            if (leaf == null) {
                return (response);
            }
            ParseState ps = new ParseState(0, chatSession, input, that, topic, leaf);
            //chatSession.matchTrace += leaf.category.getTemplate()+"\n";
            String template = leaf.getCategory().getTemplate();
            response = evalTemplate(template, ps);
            if (log.isTraceEnabled()) {
                log.trace("in AIMLProcessor.respond(), template={}, trat={}", template, that);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return response;
    }

    /**
     * capitalizeString:
     * from http://stackoverflow.com/questions/1892765/capitalize-first-char-of-each-word-in-a-string-java
     *
     * @param string the string to capitalize
     * @return the capitalized string
     */
    private static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i])) {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * explode a string into individual characters separated by one space
     *
     * @param input input string
     * @return exploded string
     */
    private static String explode(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            builder.append(" ").append(input.charAt(i));
        }
        String result = builder.toString();
        while (result.contains("  ")) {
            result = result.replace("  ", " ");
        }
        return result.trim();
    }

    // Parsing and evaluation functions:

    /**
     * evaluate the contents of an AIML tag.
     * calls recursEval on child tags.
     *
     * @param node             the current parse node.
     * @param ps               the current parse state.
     * @param ignoreAttributes tag names to ignore when evaluating the tag.
     * @return the result of evaluating the tag contents.
     */
    public String evalTagContent(Node node, ParseState ps, Set<String> ignoreAttributes) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.evalTagContent(node: {}, ps: {}, ignoreAttributes: {}", node, ps, ignoreAttributes);
            log.trace("in AIMLProcessor.evalTagContent, node string: {}", DomUtils.nodeToString(node));
        }
        StringBuilder result = new StringBuilder();
        try {
            NodeList childList = node.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (log.isTraceEnabled()) {
                    log.trace("in AIMLProcessor.evalTagContent(), child: {}", child);
                }
                if (ignoreAttributes == null || !ignoreAttributes.contains(child.getNodeName())) {
                    result.append(recursEval(child, ps));
                }
                if (log.isTraceEnabled()) {
                    log.trace("in AIMLProcessor.evalTagContent(), result: ", result);
                }
            }
        } catch (Exception e) {
            log.error("Something went wrong with evalTagContent", e);
        }

        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.evalTagContent() returning: {}", result);
        }
        return result.toString();
    }

    /**
     * pass thru generic XML (non-AIML tags, such as HTML) as unevaluated XML
     *
     * @param node current parse node
     * @param ps   current parse state
     * @return unevaluated generic XML string
     */
    public String genericXML(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.genericXML(node: {}, ps: {}", node, ps);
        }
        String evalResult = evalTagContent(node, ps, null);
        String result = unevaluatedXML(evalResult, node, ps);
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.genericXML(), returning: {}", result);
        }
        return result;
    }

    /**
     * return a string of unevaluated XML.      When the AIML parser
     * encounters an unrecognized XML tag, it simply passes through the
     * tag in XML form.  For example, if the response contains HTML
     * markup, the HTML is passed to the requesting process.    However if that
     * markup contains AIML tags, those tags are evaluated and the parser
     * builds the result.
     *
     * @param node current parse node.
     * @param ps   current parse state.
     * @return the unevaluated XML string
     */
    private static String unevaluatedXML(String resultIn, Node node, ParseState ps) {
        String nodeName = node.getNodeName();
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.unevaluatedXML(resultIn: {}, node: {}, nodeName: {}, ps: {}", resultIn, node, nodeName, ps);
        }
        StringBuilder attributes = new StringBuilder();
        if (node.hasAttributes()) {
            NamedNodeMap XMLAttributes = node.getAttributes();
            for (int i = 0; i < XMLAttributes.getLength(); i++) {
                attributes
                        .append(" ")
                        .append(XMLAttributes.item(i).getNodeName())
                        .append("=\"")
                        .append(XMLAttributes.item(i).getNodeValue())
                        .append("\"");
            }
        }
        String result = "<" + nodeName + attributes + "/>";
        if (!"".equals(resultIn)) {
            result = "<" + nodeName + attributes + ">" + resultIn + "</" + nodeName + ">";
        }
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.unevaluatedXML() returning: {}", result);
        }
        return result;
    }

    /**
     * implements AIML <srai> tag
     *
     * @param node current parse node.
     * @param ps   current parse state.
     * @return the result of processing the <srai>
     */
    private String srai(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.srai(node: {}, ps: {}", node, ps);
        }
        sraiCount++;
        if (sraiCount > bot.getConfiguration().getMaxRecursionCount()
                || ps.getDepth() > bot.getConfiguration().getMaxRecursionDepth()) {
            return bot.getConfiguration().getLanguage().getTooMuchRecursion();
        }
        String response = bot.getConfiguration().getLanguage().getDefaultResponse();
        try {
            String result = evalTagContent(node, ps, null);
            result = result.trim();
            result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
            result = ps.getChatSession().getBot().getPreProcessor().normalize(result);
            if (bot.getConfiguration().isJpTokenize()) {
                result = JapaneseUtils.tokenizeSentence(result);
            }
            String topic = ps.getChatSession().getPredicates().get("topic");     // the that stays the same, but the topic may have changed
            if (log.isTraceEnabled()) {
                log.trace("<srai>{}</srai> from {} topic={}", result, ps.getLeaf().getCategory().inputThatTopic(), topic);
            }
            Nodemapper leaf = ps.getChatSession().getBot().getBrain().match(result, ps.getThat(), topic);
            if (leaf == null) {
                return response;
            }
            if (log.isTraceEnabled()) {
                log.trace("Srai returned {}:{}, that=", leaf.getCategory().inputThatTopic(), leaf.getCategory().getTemplate());
            }
            response = evalTemplate(leaf.getCategory().getTemplate(), new ParseState(ps.getDepth() + 1, ps.getChatSession(), ps.getInput(), ps.getThat(), topic, leaf));
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        String result = response.trim();
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.srai(), returning: {}", result);
        }
        return result;
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
    private String getAttributeOrTagValue(Node node, ParseState ps, String attributeName) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.getAttributeOrTagValue (node: {}, attributeName: {})", node, attributeName);
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
            log.trace("in AIMLProcessor.getAttributeOrTagValue (), returning: {}", result);
        }
        return result;
    }

    /**
     * access external web service for response
     * implements <sraix></sraix>
     * and its attribute variations.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return response from remote service or string indicating failure.
     */
    private String sraix(Node node, ParseState ps) {
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

    /**
     * map an element of one string set to an element of another
     * Implements <map name="mapname"></map>   and <map><name>mapname</name></map>
     *
     * @param node current XML parse node
     * @param ps   current AIML parse state
     * @return the map result or a string indicating the key was not found
     */
    private String map(Node node, ParseState ps) {
        String result = Constants.default_map;
        Set<String> attributeNames = Utilities.stringSet("name");
        String mapName = getAttributeOrTagValue(node, ps, "name");
        String contents = evalTagContent(node, ps, attributeNames);
        contents = contents.trim();
        if (mapName == null) {
            result = "<map>" + contents + "</map>"; // this is an OOB map tag (no attribute)
        } else {
            AIMLMap map = ps.getChatSession().getBot().getMaps().get(mapName);
            if (map != null) {
                result = map.get(contents.toUpperCase());
            }
            if (log.isTraceEnabled()) {
                log.trace("AIMLProcessor map {} ", result);
            }
            if (result == null) {
                result = Constants.default_map;
            }
            result = result.trim();
        }
        return result;
    }

    /**
     * set the value of an AIML predicate.
     * Implements <set name="predicate"></set> and <set var="varname"></set>
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the result of the <set> operation
     */
    private String set(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.set(node: {}, ps: {})", node, ps);
        }
        Set<String> attributeNames = Utilities.stringSet("name", "var");
        String predicateName = getAttributeOrTagValue(node, ps, "name");
        String varName = getAttributeOrTagValue(node, ps, "var");
        String result = evalTagContent(node, ps, attributeNames).trim();
        result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
        String value = result.trim();
        if (predicateName != null) {
            ps.getChatSession().getPredicates().put(predicateName, result);
            if (log.isTraceEnabled()) {
                log.trace("Set predicate {} to {} in {}", predicateName, result, ps.getLeaf().getCategory().inputThatTopic());
            }
        }
        if (varName != null) {
            ps.getVars().put(varName, result);
            if (log.isTraceEnabled()) {
                log.trace("Set var {} to {} in {}", varName, value, ps.getLeaf().getCategory().inputThatTopic());
            }
        }
        if (ps.getChatSession().getBot().getPronounSet().contains(predicateName)) {
            result = predicateName;
        }
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.set, returning: {}", result);
        }
        return result;
    }

    /**
     * get the value of an AIML predicate.
     * implements <get name="predicate"></get>  and <get var="varname"></get>
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the result of the <get> operation
     */
    private String get(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.get(node: {}, ps: {})", node, ps);
        }
        String result = Constants.default_get;
        String predicateName = getAttributeOrTagValue(node, ps, "name");
        String varName = getAttributeOrTagValue(node, ps, "var");
        String tupleName = getAttributeOrTagValue(node, ps, "tuple");
        if (predicateName != null) {
            result = ps.getChatSession().getPredicates().get(predicateName).trim();
        } else if (varName != null && tupleName != null) {
            result = tupleGet(tupleName, varName);
        } else if (varName != null) {
            result = ps.getVars().get(varName).trim();
        }
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.get, returning: {}", result);
        }
        return result;
    }

    private String tupleGet(String tupleName, String varName) {
        Tuple tuple = tupleMap.get(tupleName);
        return tuple == null ? Constants.default_get : tuple.getValue(varName);
    }

    /**
     * return the value of a bot property.
     * implements {{{@code <bot name="property"/>}}}
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the bot property or a string indicating the property was not found.
     */
    private String bot(Node node, ParseState ps) {
        String result = Constants.default_property;
        String propertyName = getAttributeOrTagValue(node, ps, "name");
        if (propertyName != null) {
            result = ps.getChatSession().getBot().getProperties().get(propertyName).trim();
        }
        return result;
    }

    /**
     * implements formatted date tag <date jformat="format"/> and <date format="format"/>
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the formatted date
     */
    private String date(Node node, ParseState ps) {
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String locale = getAttributeOrTagValue(node, ps, "locale");
        String timezone = getAttributeOrTagValue(node, ps, "timezone");
        return CalendarUtils.date(jformat, locale, timezone);
    }

    /**
     * <interval><style>years</style></style><jformat>MMMMMMMMM dd, yyyy</jformat><from>August 2, 1960</from><to><date><jformat>MMMMMMMMM dd, yyyy</jformat></date></to></interval>
     */

    private String interval(Node node, ParseState ps) {
        String style = getAttributeOrTagValue(node, ps, "style");      // AIML 2.0
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String from = getAttributeOrTagValue(node, ps, "from");
        String to = getAttributeOrTagValue(node, ps, "to");
        if (style == null) {
            style = "years";
        }
        if (jformat == null) {
            jformat = "MMMMMMMMM dd, yyyy";
        }
        if (from == null) {
            from = "January 1, 1970";
        }
        if (to == null) {
            to = CalendarUtils.date(jformat, null, null);
        }
        String result = "unknown";
        if ("years".equals(style)) {
            result = "" + IntervalUtils.getYearsBetween(from, to, jformat);
        }
        if ("months".equals(style)) {
            result = "" + IntervalUtils.getMonthsBetween(from, to, jformat);
        }
        if ("days".equals(style)) {
            result = "" + IntervalUtils.getDaysBetween(from, to, jformat);
        }
        if ("hours".equals(style)) {
            result = "" + IntervalUtils.getHoursBetween(from, to, jformat);
        }
        return result;
    }

    /**
     * get the value of an index attribute and return it as an integer.
     * if it is not recognized as an integer, return 0
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the the integer intex value
     */
    private int getIndexValue(Node node, ParseState ps) {
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

    /**
     * implements {@code <star index="N"/>}
     * returns the value of input words matching the Nth wildcard (or AIML Set).
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the word sequence matching a wildcard
     */
    private String inputStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getInputStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getInputStars().star(index).trim();
    }

    /**
     * implements {@code <thatstar index="N"/>}
     * returns the value of input words matching the Nth wildcard (or AIML Set) in <that></that>.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the word sequence matching a wildcard
     */
    private String thatStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getThatStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getThatStars().star(index).trim();
    }

    /**
     * implements <topicstar/> and <topicstar index="N"/>
     * returns the value of input words matching the Nth wildcard (or AIML Set) in a topic pattern.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the word sequence matching a wildcard
     */
    private String topicStar(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getTopicStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getTopicStars().star(index).trim();
    }

    /**
     * return the client ID.
     * implements {@code <id/>}
     *
     * @param ps AIML parse state
     * @return client ID
     */

    private static String id(ParseState ps) {
        return ps.getChatSession().getCustomerId();
    }

    /**
     * return the size of the robot brain (number of AIML categories loaded).
     * implements {@code <size/>}
     *
     * @param ps AIML parse state
     * @return bot brain size
     */
    private static String size(ParseState ps) {
        int size = ps.getChatSession().getBot().getBrain().getCategories().size();
        return String.valueOf(size);
    }

    /**
     * return the size of the robot vocabulary (number of words the bot can recognize).
     * implements {@code <vocabulary/>}
     *
     * @param ps AIML parse state
     * @return bot vocabulary size
     */
    private static String vocabulary(ParseState ps) {
        int size = ps.getChatSession().getBot().getBrain().getVocabulary().size();
        return String.valueOf(size);
    }

    /**
     * return a string indicating the name and version of the AIML program.
     * implements {@code <program/>}
     *
     * @return AIML program name and version.
     */
    private String program() {
        return bot.getConfiguration().getProgramName();
    }

    /**
     * implements the (template-side) {@code <that index="M,N"/>}    tag.
     * returns a normalized sentence.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the nth last sentence of the bot's mth last reply.
     */
    private String that(Node node, ParseState ps) {
        int index = 0;
        int jndex = 0;
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                String[] spair = value.split(",");
                index = Integer.parseInt(spair[0]) - 1;
                jndex = Integer.parseInt(spair[1]) - 1;
                log.debug("That index={},{}", index, jndex);
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        String that = Constants.unknown_history_item;
        History hist = ps.getChatSession().getThatHistory().get(index);
        if (hist != null) {
            that = (String) hist.get(jndex);
        }
        return that.trim();
    }

    /**
     * implements {@code <input index="N"/>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the nth last sentence input to the bot
     */
    private String input(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getInputHistory().getString(index);
    }

    /**
     * implements {@code <request index="N"/>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the nth last multi-sentence request to the bot.
     */
    private String request(Node node, ParseState ps) {             // AIML 2.0
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getRequestHistory().getString(index).trim();
    }

    /**
     * implements {@code <response index="N"/>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the bot's Nth last multi-sentence response.
     */
    private String response(Node node, ParseState ps) {            // AIML 2.0
        int index = getIndexValue(node, ps);
        return ps.getChatSession().getResponseHistory().getString(index).trim();
    }

    /**
     * implements {@code <system>} tag.
     * Evaluate the contents, and try to execute the result as
     * a command in the underlying OS shell.
     * Read back and return the result of this command.
     * <p>
     * The timeout parameter allows the botmaster to set a timeout
     * in ms, so that the <system></system>   command returns eventually.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return the result of executing the system command or a string indicating the command failed.
     */
    private String system(Node node, ParseState ps) {
        if (!bot.getConfiguration().isEnableSystemTag()) {
            return "";
        }
        Set<String> attributeNames = Utilities.stringSet("timeout");
        String evaluatedContents = evalTagContent(node, ps, attributeNames);
        return IOUtils.system(evaluatedContents, bot.getConfiguration().getLanguage().getSystemFailed());
    }

    /**
     * implements {@code <think>} tag
     * <p>
     * Evaluate the tag contents but return a blank.
     * "Think but don't speak."
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return a blank empty string
     */
    private String think(Node node, ParseState ps) {
        evalTagContent(node, ps, null);
        return "";
    }

    /**
     * Transform a string of words (separtaed by spaces) into
     * a string of individual characters (separated by spaces).
     * Explode "ABC DEF" = "A B C D E F".
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return exploded string
     */
    private String explode(Node node, ParseState ps) {              // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return explode(result);
    }

    /**
     * apply the AIML normalization pre-processor to the evaluated tag contenst.
     * implements {@code <normalize>} tag.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return normalized string
     */
    private String normalize(Node node, ParseState ps) {            // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().getBot().getPreProcessor().normalize(result);
    }

    /**
     * apply the AIML denormalization pre-processor to the evaluated tag contenst.
     * implements {@code <normalize>} tag.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return denormalized string
     */
    private String denormalize(Node node, ParseState ps) {            // AIML 2.0
        String result = evalTagContent(node, ps, null);
        return ps.getChatSession().getBot().getPreProcessor().denormalize(result);
    }

    /**
     * evaluate tag contents and return result in upper case
     * implements {@code <uppercase>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return uppercase string
     */
    private String uppercase(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toUpperCase();
    }

    /**
     * evaluate tag contents and return result in lower case
     * implements {@code <lowercase>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return lowercase string
     */
    private String lowercase(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return result.toLowerCase();
    }

    /**
     * evaluate tag contents and capitalize each word.
     * implements {@code <formal>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return capitalized string
     */
    private String formal(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return capitalizeString(result);
    }

    /**
     * evaluate tag contents and capitalize the first word.
     * implements {@code <sentence>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return string with first word capitalized
     */
    private String sentence(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        if (result.length() > 1) {
            return result.substring(0, 1).toUpperCase() + result.substring(1, result.length());
        }
        return "";
    }

    /**
     * evaluate tag contents and swap 1st and 2nd person pronouns
     * implements {@code <person>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return sentence with pronouns swapped
     */
    private String person(Node node, ParseState ps) {
        String result;
        if (node.hasChildNodes()) {
            result = evalTagContent(node, ps, null);
        } else {
            result = ps.getStarBindings().getInputStars().star(0);   // for <person/>
        }
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().person(result);
        return result.trim();
    }

    /**
     * evaluate tag contents and swap 1st and 3rd person pronouns
     * implements {@code <person2>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return sentence with pronouns swapped
     */
    private String person2(Node node, ParseState ps) {
        String result;
        if (node.hasChildNodes()) {
            result = evalTagContent(node, ps, null);
        } else {
            result = ps.getStarBindings().getInputStars().star(0);   // for <person2/>
        }
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().person2(result);
        return result.trim();
    }

    /**
     * implements {@code <gender>} tag
     * swaps gender pronouns
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return sentence with gender ronouns swapped
     */
    private String gender(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        result = " " + result + " ";
        result = ps.getChatSession().getBot().getPreProcessor().gender(result);
        return result.trim();
    }

    /**
     * implements {@code <random>} tag
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return response randomly selected from the list
     */
    private String random(Node node, ParseState ps) {
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

    private String unevaluatedAIML(Node node, ParseState ps) {
        String result = learnEvalTagContent(node, ps);
        return unevaluatedXML(result, node, ps);
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

    private String learn(Node node, ParseState ps) {                 // learn, learnf AIML 2.0
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
                Category c;
                if (node.getNodeName().equals("learn")) {
                    c = new Category(bot, 0, pattern, that, "*", template, Constants.nullAimlFile);
                    ps.getChatSession().getBot().getLearnGraph().addCategory(c);
                } else {// learnf
                    c = new Category(bot, 0, pattern, that, "*", template, Constants.learnfAimlFile);
                    ps.getChatSession().getBot().getLearnfGraph().addCategory(c);
                }
                ps.getChatSession().getBot().getBrain().addCategory(c);
            }
        }
        return "";
    }

    /**
     * implements {@code <condition> with <loop/>}
     * re-evaluate the conditional statement until the response does not contain {@code <loop/>}
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return result of conditional expression
     */
    private String loopCondition(Node node, ParseState ps) {
        boolean loop = true;
        StringBuilder result = new StringBuilder();
        int loopCnt = 0;
        while (loop && loopCnt < bot.getConfiguration().getMaxLoops()) {
            String loopResult = condition(node, ps);
            String tooMuch = bot.getConfiguration().getLanguage().getTooMuchRecursion();
            if (loopResult.trim().equals(tooMuch)) {
                return tooMuch;
            }
            if (loopResult.contains("<loop/>")) {
                loopResult = loopResult.replace("<loop/>", "");
                loop = true;
            } else {
                loop = false;
            }
            result.append(loopResult);
        }
        return loopCnt >= bot.getConfiguration().getMaxLoops()
                ? bot.getConfiguration().getLanguage().getTooMuchLooping()
                : result.toString();
    }

    /**
     * implements all 3 forms of the {@code <condition> tag}
     * In AIML 2.0 the conditional may return a {@code <loop/>}
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     * @return result of conditional expression
     */
    private String condition(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<>();
        String predicate, varName, value; //Node p=null, v=null;
        Set<String> attributeNames = Utilities.stringSet("name", "var", "value");
        // First check if the <condition> has an attribute "name".  If so, get the predicate name.
        predicate = getAttributeOrTagValue(node, ps, "name");
        varName = getAttributeOrTagValue(node, ps, "var");
        // Make a list of all the <li> child nodes:
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals("li")) liList.add(childList.item(i));
        }
        if (liList.size() == 0 && (value = getAttributeOrTagValue(node, ps, "value")) != null &&
                predicate != null &&
                ps.getChatSession().getPredicates().get(predicate).equalsIgnoreCase(value)) {
            return evalTagContent(node, ps, attributeNames);
        } else if (liList.size() == 0 && (value = getAttributeOrTagValue(node, ps, "value")) != null &&
                varName != null &&
                ps.getVars().get(varName).equalsIgnoreCase(value)) {
            return evalTagContent(node, ps, attributeNames);
        } else {
            for (Node n : liList) {
                String liPredicate = predicate;
                String liVarName = varName;
                if (liPredicate == null) {
                    liPredicate = getAttributeOrTagValue(n, ps, "name");
                }
                if (liVarName == null) {
                    liVarName = getAttributeOrTagValue(n, ps, "var");
                }
                value = getAttributeOrTagValue(n, ps, "value");
                if (value != null) {
                    // if the predicate equals the value, return the <li> item.
                    if (liPredicate != null && (ps.getChatSession().getPredicates().get(liPredicate).equalsIgnoreCase(value) ||
                            (ps.getChatSession().getPredicates().containsKey(liPredicate) && value.equals("*")))) {
                        return evalTagContent(n, ps, attributeNames);
                    } else if (liVarName != null && (ps.getVars().get(liVarName).equalsIgnoreCase(value) ||
                            (ps.getVars().containsKey(liPredicate) && value.equals("*")))) {
                        return evalTagContent(n, ps, attributeNames);
                    }
                } else {
                    return evalTagContent(n, ps, attributeNames);
                }
            }
        }
        return "";
    }

    /**
     * check to see if a result contains a {@code <loop/>} tag.
     *
     * @param node current XML parse node
     * @return true or false
     */
    public static boolean evalTagForLoop(Node node) {
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals("loop")) {
                return true;
            }
        }
        return false;
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

    private String uniq(Node node, ParseState ps) {
        HashSet<String> vars = new HashSet<>();
        HashSet<String> visibleVars = new HashSet<>();
        String subj = "?subject";
        String pred = "?predicate";
        String obj = "?object";
        NodeList childList = node.getChildNodes();
        for (int j = 0; j < childList.getLength(); j++) {
            Node childNode = childList.item(j);
            String contents = evalTagContent(childNode, ps, null);
            if (childNode.getNodeName().equals("subj")) {
                subj = contents;
            } else if (childNode.getNodeName().equals("pred")) {
                pred = contents;
            } else if (childNode.getNodeName().equals("obj")) {
                obj = contents;
            }
            if (contents.startsWith("?")) {
                visibleVars.add(contents);
                vars.add(contents);
            }
        }
        Tuple partial = storeTuple(new Tuple(vars, visibleVars));
        Clause clause = new Clause(subj, pred, obj);
        Set<Tuple> tuples = ps.getChatSession().getTripleStore().selectFromSingleClause(partial, clause, true);
        String tupleList = tuples.stream().map(Tuple::getName).collect(Collectors.joining(" "));
        if (tupleList.length() == 0) {
            tupleList = "NIL";
        }
        String var = "";
        for (String x : visibleVars) {
            var = x;
        }
        String firstTuple = firstWord(tupleList);
        return tupleGet(firstTuple, var);
    }

    public Tuple storeTuple(Tuple tuple) {
        tupleMap.put(tuple.getName(), tuple);
        return tuple;
    }

    public String select(Node node, ParseState ps) {
        ArrayList<Clause> clauses = new ArrayList<>();
        NodeList childList = node.getChildNodes();
        //String[] splitTuple;
        HashSet<String> vars = new HashSet<>();
        HashSet<String> visibleVars = new HashSet<>();
        for (int i = 0; i < childList.getLength(); i++) {
            Node childNode = childList.item(i);
            if (childNode.getNodeName().equals("vars")) {
                String contents = evalTagContent(childNode, ps, null);
                String[] splitVars = contents.split(" ");
                for (String var : splitVars) {
                    var = var.trim();
                    if (var.length() > 0) {
                        visibleVars.add(var);
                    }
                }
            } else if (childNode.getNodeName().equals("q") || childNode.getNodeName().equals("notq")) {
                Boolean affirm = !childNode.getNodeName().equals("notq");
                NodeList grandChildList = childNode.getChildNodes();
                String subj = null;
                String pred = null;
                String obj = null;
                for (int j = 0; j < grandChildList.getLength(); j++) {
                    Node grandChildNode = grandChildList.item(j);
                    String contents = evalTagContent(grandChildNode, ps, null);
                    if (grandChildNode.getNodeName().equals("subj")) {
                        subj = contents;
                    } else if (grandChildNode.getNodeName().equals("pred")) {
                        pred = contents;
                    } else if (grandChildNode.getNodeName().equals("obj")) {
                        obj = contents;
                    }
                    if (contents.startsWith("?")) {
                        vars.add(contents);
                    }
                }
                clauses.add(new Clause(subj, pred, obj, affirm));
            }
        }
        Set<Tuple> tuples = ps.getChatSession().getTripleStore().select(vars, visibleVars, clauses);
        String result = tuples.stream().map(Tuple::getName).collect(Collectors.joining(" "));
        if (result.length() == 0) {
            result = "NIL";
        }
        return result;
    }

    public String subject(Node node, ParseState ps) {
        String id = evalTagContent(node, ps, null);
        TripleStore ts = ps.getChatSession().getTripleStore();
        String subject = "unknown";
        if (ts.getIdTriple().containsKey(id)) {
            subject = ts.getIdTriple().get(id).getSubject();
        }
        return subject;
    }

    public String predicate(Node node, ParseState ps) {
        String id = evalTagContent(node, ps, null);
        TripleStore ts = ps.getChatSession().getTripleStore();
        if (ts.getIdTriple().containsKey(id)) {
            return ts.getIdTriple().get(id).getPredicate();
        }
        return "unknown";
    }

    public String object(Node node, ParseState ps) {
        String id = evalTagContent(node, ps, null);
        TripleStore ts = ps.getChatSession().getTripleStore();
        if (ts.getIdTriple().containsKey(id)) {
            return ts.getIdTriple().get(id).getObject();
        }
        return "unknown";
    }

    public String javascript(Node node, ParseState ps) {
        String result = Constants.bad_javascript;
        String script = evalTagContent(node, ps, null);
        try {
            result = IOUtils.evalScript("JavaScript", script);
        } catch (Exception e) {
            log.error("JavaScript error:", e);
        }
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.javascript, returning result: {}", result);
        }
        return result;
    }

    private static String firstWord(String sentence) {
        String content = (sentence == null ? "" : sentence);
        content = content.trim();
        if (content.contains(" ")) {
            return content.substring(0, content.indexOf(" "));
        } else if (content.length() > 0) {
            return content;
        }
        return Constants.default_list_item;
    }

    private static String restWords(String sentence) {
        String content = (sentence == null ? "" : sentence);
        content = content.trim();
        if (content.contains(" ")) {
            return content.substring(content.indexOf(" ") + 1, content.length());
        }
        return Constants.default_list_item;
    }

    public String first(Node node, ParseState ps) {
        String content = evalTagContent(node, ps, null);
        return firstWord(content);
    }

    public String rest(Node node, ParseState ps) {
        String content = evalTagContent(node, ps, null);
        content = ps.getChatSession().getBot().getPreProcessor().normalize(content);
        return restWords(content);
    }

    private static String resetlearnf(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnfCategories();
        return "Deleted Learnf Categories";
    }

    private static String resetlearn(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnCategories();
        return "Deleted Learn Categories";
    }

    /**
     * Recursively descend the XML DOM tree, evaluating AIML and building a response.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     */
    private String recursEval(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.recursEval(node: {}, ps: {})", node, ps);
        }
        try {
            String nodeName = node.getNodeName();
            switch (nodeName) {
                case "#text":
                    return node.getNodeValue();
                case "#comment":
                    return "";
                case "template":
                    return evalTagContent(node, ps, null);
                case "random":
                    return random(node, ps);
                case "condition":
                    return loopCondition(node, ps);
                case "srai":
                    return srai(node, ps);
                case "sr":
                    return respond(ps.getStarBindings().getInputStars().star(0), ps.getThat(), ps.getTopic(), ps.getChatSession(), sraiCount);
                case "sraix":
                    return sraix(node, ps);
                case "set":
                    return set(node, ps);
                case "get":
                    return get(node, ps);
                case "map": // AIML 2.0 -- see also <set> in pattern
                    return map(node, ps);
                case "bot":
                    return bot(node, ps);
                case "id":
                    return id(ps);
                case "size":
                    return size(ps);
                case "vocabulary":
                    return vocabulary(ps);
                case "program":
                    return program();
                case "date":
                    return date(node, ps);
                case "interval":
                    return interval(node, ps);
                case "think":
                    return think(node, ps);
                case "system":
                    return system(node, ps);
                case "explode":
                    return explode(node, ps);
                case "normalize":
                    return normalize(node, ps);
                case "denormalize":
                    return denormalize(node, ps);
                case "uppercase":
                    return uppercase(node, ps);
                case "lowercase":
                    return lowercase(node, ps);
                case "formal":
                    return formal(node, ps);
                case "sentence":
                    return sentence(node, ps);
                case "person":
                    return person(node, ps);
                case "person2":
                    return person2(node, ps);
                case "gender":
                    return gender(node, ps);
                case "star":
                    return inputStar(node, ps);
                case "thatstar":
                    return thatStar(node, ps);
                case "topicstar":
                    return topicStar(node, ps);
                case "that":
                    return that(node, ps);
                case "input":
                    return input(node, ps);
                case "request":
                    return request(node, ps);
                case "response":
                    return response(node, ps);
                case "learn":
                case "learnf":
                    return learn(node, ps);
                case "addtriple":
                    return addTriple(node, ps);
                case "deletetriple":
                    return deleteTriple(node, ps);
                case "javascript":
                    return javascript(node, ps);
                case "select":
                    return select(node, ps);
                case "uniq":
                    return uniq(node, ps);
                case "first":
                    return first(node, ps);
                case "rest":
                    return rest(node, ps);
                case "resetlearnf":
                    return resetlearnf(ps);
                case "resetlearn":
                    return resetlearn(ps);
                default:
                    if (extensions != null) {
                        for (AIMLProcessorExtension extension : extensions) {
                            if (extension != null && extension.extensionTagSet().contains(nodeName)) {
                                return extension.recursEval(node, ps);
                            }
                        }
                    }
                    return genericXML(node, ps);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }

    /**
     * evaluate an AIML template expression
     *
     * @param template AIML template contents
     * @param ps       AIML Parse state
     * @return result of evaluating template.
     */
    private String evalTemplate(String template, ParseState ps) {
        try {
            template = "<template>" + template + "</template>";
            Node root = DomUtils.parseString(template);
            return recursEval(root, ps);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return bot.getConfiguration().getLanguage().getTemplateFailed();
    }

    /**
     * check to see if a template is a valid XML expression.
     *
     * @param template AIML template contents
     * @return true or false.
     */
    public static boolean validTemplate(String template) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.validTemplate(template: {})", template);
        }
        try {
            template = "<template>" + template + "</template>";
            DomUtils.parseString(template);
            return true;
        } catch (Exception e) {
            log.error("Invalid Template {}", template, e);
        }
        return false;
    }

    public void registerExtension(AIMLProcessorExtension extension) {
        this.extensions.add(extension);
        extension.setProcessor(this);
    }
}
