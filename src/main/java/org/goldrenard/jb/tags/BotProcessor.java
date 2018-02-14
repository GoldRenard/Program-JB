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

/**
 * return the value of a bot property.
 * implements {{{@code <bot name="property"/>}}}
 */
public class BotProcessor extends BaseTagProcessor {

    public BotProcessor() {
        super("bot");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = Constants.default_property;
        String propertyName = getAttributeOrTagValue(node, ps, "name");
        if (propertyName != null) {
            if (ps.getRequest() != null && ps.getRequest().getAttributes() != null) {
                Object attribute = ps.getRequest().getAttributes().get(propertyName);
                if (attribute != null) {
                    result = String.valueOf(attribute);
                }
            }
            if (result == null || Constants.default_property.equals(result)) {
                result = ps.getChatSession().getBot().getProperties().get(propertyName).trim();
            }
        }
        return result;
    }
}
