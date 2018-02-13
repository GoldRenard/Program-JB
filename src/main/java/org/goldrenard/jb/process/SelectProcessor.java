package org.goldrenard.jb.process;

import org.goldrenard.jb.model.Clause;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.model.Tuple;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectProcessor extends BaseNodeProcessor {

    public SelectProcessor(AIMLProcessor processor) {
        super(processor, "select");
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
