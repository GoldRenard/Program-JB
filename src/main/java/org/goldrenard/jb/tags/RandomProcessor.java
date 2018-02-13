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
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * implements {@code <random>} tag
 */
public class RandomProcessor extends BaseTagProcessor {

    public RandomProcessor() {
        super("random");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<>();
        for (int i = 0; i < childList.getLength(); i++) {
            if (childList.item(i).getNodeName().equals("li")) liList.add(childList.item(i));
        }
        int index = (int) (Math.random() * liList.size());
        if (ps.getChatSession() != null && ps.getChatSession().getBot().getConfiguration().isQaTestMode()) {
            index = 0;
        }
        return evalTagContent(liList.get(index), ps, null);
    }
}
