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

import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.tags.base.BaseTagProcessor;
import org.goldrenard.jb.utils.IOUtils;
import org.goldrenard.jb.utils.Utilities;
import org.w3c.dom.Node;

import java.util.Set;

/**
 * implements {@code <system>} tag.
 * Evaluate the contents, and try to execute the result as
 * a command in the underlying OS shell.
 * Read back and return the result of this command.
 * <p>
 * The timeout parameter allows the botmaster to set a timeout
 * in ms, so that the <system></system>   command returns eventually.
 */
public class SystemProcessor extends BaseTagProcessor {

    public SystemProcessor() {
        super("system");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        Bot bot = ps.getChatSession().getBot();
        if (!bot.getConfiguration().isEnableSystemTag()) {
            return "";
        }
        Set<String> attributeNames = Utilities.stringSet("timeout");
        String evaluatedContents = evalTagContent(node, ps, attributeNames);
        return IOUtils.system(evaluatedContents, bot.getConfiguration().getLanguage().getSystemFailed());
    }
}
