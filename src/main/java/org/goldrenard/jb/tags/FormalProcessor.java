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
 * evaluate tag contents and capitalize each word.
 * implements {@code <formal>} tag
 */
public class FormalProcessor extends BaseTagProcessor {

    public FormalProcessor() {
        super("formal");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = evalTagContent(node, ps, null);
        return capitalizeString(result);
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
}
