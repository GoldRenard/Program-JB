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
package org.goldrenard.jb.tags;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.goldrenard.jb.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Set;

public class PredicateProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(PredicateProcessor.class);

    public PredicateProcessor() {
        super("get", "set");
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
            log.trace("PredicateProcessor.set(node: {}, ps: {})", node, ps);
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
        if (ps.getChatSession().getBot().getPronouns().contains(predicateName)) {
            result = predicateName;
        }
        if (log.isTraceEnabled()) {
            log.trace("in PredicateProcessor.set, returning: {}", result);
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
            log.trace("PredicateProcessor.get(node: {}, ps: {})", node, ps);
        }
        String result = Constants.default_get;
        String predicateName = getAttributeOrTagValue(node, ps, "name");
        String varName = getAttributeOrTagValue(node, ps, "var");
        String tupleName = getAttributeOrTagValue(node, ps, "tuple");
        if (predicateName != null) {
            result = ps.getChatSession().getPredicates().get(predicateName).trim();
        } else if (varName != null && tupleName != null) {
            result = ps.getChatSession().getTripleStore().tupleGet(tupleName, varName);
        } else if (varName != null) {
            result = ps.getVars().get(varName).trim();
        }
        if (log.isTraceEnabled()) {
            log.trace("in PredicateProcessor.get, returning: {}", result);
        }
        return result;
    }

    @Override
    public String eval(Node node, ParseState ps) {
        try {
            switch (node.getNodeName()) {
                case "set":
                    return set(node, ps);
                case "get":
                    return get(node, ps);
                default:
                    throw new IllegalStateException("Unsupported tag");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }
}
