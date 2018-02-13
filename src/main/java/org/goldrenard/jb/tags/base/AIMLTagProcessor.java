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
package org.goldrenard.jb.tags.base;

import org.goldrenard.jb.model.ParseState;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * An interface that handles node
 */
public interface AIMLTagProcessor {

    /**
     * Evaluates node
     *
     * @param node Node
     * @param ps   Parse state
     * @return Evaluation result
     */
    String eval(Node node, ParseState ps);

    /**
     * Provide a list of extension tag names supported by this processor
     *
     * @return Set of extension tag names supported by this processor
     */
    Set<String> getTags();

}
