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

import org.goldrenard.jb.core.Sraix;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.goldrenard.jb.utils.Utilities;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * access external web service for response
 * implements <sraix></sraix>
 * and its attribute variations.
 */
public class SraixProcessor extends BaseTagProcessor {

    public SraixProcessor() {
        super("sraix");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        Set<String> attributeNames = Utilities.stringSet("botid", "host");
        String host = getAttributeOrTagValue(node, ps, "host");
        String botid = getAttributeOrTagValue(node, ps, "botid");
        String hint = getAttributeOrTagValue(node, ps, "hint");
        String limit = getAttributeOrTagValue(node, ps, "limit");
        String defaultResponse = getAttributeOrTagValue(node, ps, "default");
        String evalResult = evalTagContent(node, ps, attributeNames);
        return Sraix.sraix(ps.getChatSession(), ps.getChatSession().getBot(),
                evalResult, defaultResponse, hint, host, botid, null, limit);
    }
}
