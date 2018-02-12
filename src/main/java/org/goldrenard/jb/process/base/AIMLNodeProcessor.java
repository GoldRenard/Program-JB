package org.goldrenard.jb.process.base;

import org.goldrenard.jb.model.ParseState;
import org.w3c.dom.Node;

import java.util.Set;

public interface AIMLNodeProcessor {

    String eval(Node node, ParseState ps);

    /**
     * provide the AIMLProcessor with a list of extension tag names.
     *
     * @return Set of extension tag names
     */
    Set<String> getTags();

}
