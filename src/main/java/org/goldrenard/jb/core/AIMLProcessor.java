/*
 * This file is part of Program JB.
 *
 * Program JB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Program JB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Program JB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.goldrenard.jb.core;

import org.apache.commons.lang3.StringUtils;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.Category;
import org.goldrenard.jb.model.Nodemapper;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.*;
import org.goldrenard.jb.tags.base.AIMLTagProcessor;
import org.goldrenard.jb.utils.DomUtils;
import org.goldrenard.jb.utils.JapaneseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * The core AIML parser and interpreter.
 * Implements the AIML 2.0 specification as described in
 * AIML 2.0 Working Draft document
 * https://docs.google.com/document/d/1wNT25hJRyupcG51aO89UcQEiG-HkXRXusukADpFnDs4/pub
 */
public class AIMLProcessor {

    private static final Logger log = LoggerFactory.getLogger(AIMLProcessor.class);

    private Map<String, AIMLTagProcessor> nodeProcessors = new HashMap<>();

    private final Bot bot;

    public AIMLProcessor(Bot bot) {
        this.bot = bot;
        registerProcessor(new BotProcessor());
        registerProcessor(new CommentProcessor());
        registerProcessor(new ConditionProcessor());
        registerProcessor(new DateProcessor());
        registerProcessor(new DenormalizeProcessor());
        registerProcessor(new ExplodeProcessor());
        registerProcessor(new FirstProcessor());
        registerProcessor(new FormalProcessor());
        registerProcessor(new GenderProcessor());
        registerProcessor(new IdProcessor());
        registerProcessor(new InputProcessor());
        registerProcessor(new IntervalProcessor());
        registerProcessor(new JavaScriptProcessor());
        registerProcessor(new LearnProcessor());
        registerProcessor(new LowercaseProcessor());
        registerProcessor(new MapProcessor());
        registerProcessor(new NormalizeProcessor());
        registerProcessor(new Person2Processor());
        registerProcessor(new PersonProcessor());
        registerProcessor(new PredicateProcessor());
        registerProcessor(new ProgramProcessor());
        registerProcessor(new RandomProcessor());
        registerProcessor(new RequestProcessor());
        registerProcessor(new ResetLearnProcessor());
        registerProcessor(new ResponseProcessor());
        registerProcessor(new RestProcessor());
        registerProcessor(new SelectProcessor());
        registerProcessor(new SentenceProcessor());
        registerProcessor(new SizeProcessor());
        registerProcessor(new SraiProcessor());
        registerProcessor(new SraixProcessor());
        registerProcessor(new SrProcessor());
        registerProcessor(new StarProcessor());
        registerProcessor(new SystemProcessor());
        registerProcessor(new TemplateProcessor());
        registerProcessor(new TextProcessor());
        registerProcessor(new ThatProcessor());
        registerProcessor(new ThatStarProcessor());
        registerProcessor(new ThinkProcessor());
        registerProcessor(new TopicStarProcessor());
        registerProcessor(new TripleProcessor());
        registerProcessor(new UniqProcessor());
        registerProcessor(new UppercaseProcessor());
        registerProcessor(new VocabularyProcessor());
        List<AIMLTagProcessor> extensions = bot.getConfiguration().getTagProcessors();
        if (extensions != null) {
            extensions.forEach(this::registerProcessor);
        }
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
        response = chatSession.getBot().getConfiguration().getLanguage().getDefaultResponse();
        try {
            Nodemapper leaf = chatSession.getBot().getBrain().match(input, that, topic);
            if (leaf == null) {
                return (response);
            }
            ParseState ps = new ParseState(this, 0, chatSession, input, that, topic, leaf, srCnt);
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
    public static String unevaluatedXML(String resultIn, Node node, ParseState ps) {
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
     * Recursively descend the XML DOM tree, evaluating AIML and building a response.
     *
     * @param node current XML parse node
     * @param ps   AIML parse state
     */
    public String recursEval(Node node, ParseState ps) {
        if (log.isTraceEnabled()) {
            log.trace("AIMLProcessor.recursEval(node: {}, ps: {})", node, ps);
        }
        try {
            String nodeName = node.getNodeName();
            AIMLTagProcessor processor = nodeProcessors.get(nodeName);
            if (processor != null && processor.getTags().contains(nodeName)) {
                return processor.eval(node, ps);
            }
            return genericXML(node, ps);
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
    public String evalTemplate(String template, ParseState ps) {
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

    public void registerProcessor(AIMLTagProcessor processor) {
        for (String tag : processor.getTags()) {
            if (nodeProcessors.containsKey(tag)) {
                log.warn("Tag <{}> handler {} has been replaced by {}", tag, nodeProcessors.get(tag), processor);
            }
            this.nodeProcessors.put(tag, processor);
        }
    }
}
