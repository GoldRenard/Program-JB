package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.w3c.dom.Node;

/**
 * Transform a string of words (separtaed by spaces) into
 * a string of individual characters (separated by spaces).
 * Explode "ABC DEF" = "A B C D E F".
 */
public class ExplodeProcessor extends BaseNodeProcessor {

    public ExplodeProcessor(AIMLProcessor processor) {
        super(processor, "explode");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return explode(result);
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
}
