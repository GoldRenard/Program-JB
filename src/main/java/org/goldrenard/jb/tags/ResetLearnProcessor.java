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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class ResetLearnProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(ResetLearnProcessor.class);

    public ResetLearnProcessor() {
        super("resetlearnf", "resetlearn");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        try {
            switch (node.getNodeName()) {
                case "resetlearnf":
                    return resetlearnf(ps);
                case "resetlearn":
                    return resetlearn(ps);
                default:
                    throw new IllegalStateException("Unsupported tag");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }

    private static String resetlearnf(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnfCategories();
        return "Deleted Learnf Categories";
    }

    private static String resetlearn(ParseState ps) {
        ps.getChatSession().getBot().deleteLearnCategories();
        return "Deleted Learn Categories";
    }
}
