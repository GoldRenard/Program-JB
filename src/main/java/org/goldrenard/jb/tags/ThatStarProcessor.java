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
 * implements {@code <thatstar index="N"/>}
 * returns the value of input words matching the Nth wildcard (or AIML Set) in <that></that>.
 */
public class ThatStarProcessor extends BaseTagProcessor {

    public ThatStarProcessor() {
        super("thatstar");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = getIndexValue(node, ps);
        if (ps.getStarBindings().getThatStars().star(index) == null) {
            return "";
        }
        return ps.getStarBindings().getThatStars().star(index).trim();
    }
}
