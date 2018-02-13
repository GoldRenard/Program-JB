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
import org.goldrenard.jb.model.AIMLMap;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.goldrenard.jb.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * map an element of one string set to an element of another
 * Implements <map name="mapname"></map>   and <map><name>mapname</name></map>
 */
public class MapProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(MapProcessor.class);

    public MapProcessor() {
        super("map");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = Constants.default_map;
        Set<String> attributeNames = Utilities.stringSet("name");
        String mapName = getAttributeOrTagValue(node, ps, "name");
        String contents = evalTagContent(node, ps, attributeNames);
        contents = contents.trim();
        if (mapName == null) {
            result = "<map>" + contents + "</map>"; // this is an OOB map tag (no attribute)
        } else {
            AIMLMap map = ps.getChatSession().getBot().getMaps().get(mapName);
            if (map != null) {
                result = map.get(contents.toUpperCase());
            }
            if (log.isTraceEnabled()) {
                log.trace("AIMLProcessor map {} ", result);
            }
            if (result == null) {
                result = Constants.default_map;
            }
            result = result.trim();
        }
        return result;
    }
}
