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
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.w3c.dom.Node;

public class RestProcessor extends BaseTagProcessor {

    public RestProcessor() {
        super("rest");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String content = evalTagContent(node, ps, null);
        content = ps.getChatSession().getBot().getPreProcessor().normalize(content);
        return restWords(content);
    }

    private static String restWords(String sentence) {
        String content = (sentence == null ? "" : sentence);
        content = content.trim();
        if (content.contains(" ")) {
            return content.substring(content.indexOf(" ") + 1, content.length());
        }
        return Constants.default_list_item;
    }
}
