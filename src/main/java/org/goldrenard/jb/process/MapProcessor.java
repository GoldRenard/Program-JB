package org.goldrenard.jb.process;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.AIMLMap;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * map an element of one string set to an element of another
 * Implements <map name="mapname"></map>   and <map><name>mapname</name></map>
 */
public class MapProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(MapProcessor.class);

    public MapProcessor(AIMLProcessor processor) {
        super(processor, "map");
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
