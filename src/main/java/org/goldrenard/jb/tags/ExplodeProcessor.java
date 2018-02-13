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

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.w3c.dom.Node;

/**
 * Transform a string of words (separtaed by spaces) into
 * a string of individual characters (separated by spaces).
 * Explode "ABC DEF" = "A B C D E F".
 */
public class ExplodeProcessor extends BaseTagProcessor {

    public ExplodeProcessor() {
        super("explode");
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
