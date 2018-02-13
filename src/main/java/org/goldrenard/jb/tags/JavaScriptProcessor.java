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
import org.goldrenard.jb.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * return the client ID.
 * implements {@code <id/>}
 */
public class JavaScriptProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptProcessor.class);

    public JavaScriptProcessor() {
        super("javascript");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String result = Constants.bad_javascript;
        String script = evalTagContent(node, ps, null);
        try {
            result = IOUtils.evalScript("JavaScript", script);
        } catch (Exception e) {
            log.error("JavaScript error:", e);
        }
        if (log.isTraceEnabled()) {
            log.trace("in AIMLProcessor.javascript, returning result: {}", result);
        }
        return result;
    }
}
