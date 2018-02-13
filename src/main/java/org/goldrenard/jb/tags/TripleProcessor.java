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

public class TripleProcessor extends BaseTagProcessor {

    private static final Logger log = LoggerFactory.getLogger(TripleProcessor.class);

    public TripleProcessor() {
        super("addtriple", "deletetriple");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        try {
            switch (node.getNodeName()) {
                case "addtriple":
                    return addTriple(node, ps);
                case "deletetriple":
                    return deleteTriple(node, ps);
                default:
                    throw new IllegalStateException("Unsupported tag");
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return "";
        }
    }

    private String deleteTriple(Node node, ParseState ps) {
        String subject = getAttributeOrTagValue(node, ps, "subj");
        String predicate = getAttributeOrTagValue(node, ps, "pred");
        String object = getAttributeOrTagValue(node, ps, "obj");
        return ps.getChatSession().getTripleStore().deleteTriple(subject, predicate, object);
    }

    private String addTriple(Node node, ParseState ps) {
        String subject = getAttributeOrTagValue(node, ps, "subj");
        String predicate = getAttributeOrTagValue(node, ps, "pred");
        String object = getAttributeOrTagValue(node, ps, "obj");
        return ps.getChatSession().getTripleStore().addTriple(subject, predicate, object);
    }
}
