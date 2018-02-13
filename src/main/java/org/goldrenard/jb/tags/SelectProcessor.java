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

import org.goldrenard.jb.model.Clause;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.model.Tuple;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectProcessor extends BaseTagProcessor {

    public SelectProcessor() {
        super("select");
    }

    @Override
    public String eval(Node node, ParseState ps) {
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
}
