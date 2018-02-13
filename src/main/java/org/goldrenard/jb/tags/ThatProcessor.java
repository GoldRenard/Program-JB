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

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.History;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * implements the (template-side) {@code <that index="M,N"/>}    tag.
 * returns a normalized sentence.
 */
public class ThatProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThatProcessor.class);

    public ThatProcessor() {
        super("that");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        int index = 0;
        int jndex = 0;
        String value = getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                String[] spair = value.split(",");
                index = Integer.parseInt(spair[0]) - 1;
                jndex = Integer.parseInt(spair[1]) - 1;
                log.debug("That index={},{}", index, jndex);
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        String that = Constants.unknown_history_item;
        History hist = ps.getChatSession().getThatHistory().get(index);
        if (hist != null) {
            that = (String) hist.get(jndex);
        }
        return that.trim();
    }
}
