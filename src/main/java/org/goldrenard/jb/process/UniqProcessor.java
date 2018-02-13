package org.goldrenard.jb.process;

import org.goldrenard.jb.model.Clause;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.model.Tuple;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UniqProcessor extends BaseNodeProcessor {

    public UniqProcessor(AIMLProcessor processor) {
        super(processor, "uniq");
    }

    @Override
    public String eval(Node node, ParseState ps) {
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
        Tuple partial = ps.getChatSession().getTripleStore().storeTuple(new Tuple(vars, visibleVars));
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
        return ps.getChatSession().getTripleStore().tupleGet(firstTuple, var);
    }
}
