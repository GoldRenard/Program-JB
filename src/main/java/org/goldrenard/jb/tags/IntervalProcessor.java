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
import org.goldrenard.jb.utils.CalendarUtils;
import org.goldrenard.jb.utils.IntervalUtils;
import org.w3c.dom.Node;

/**
 * <interval><style>years</style></style><jformat>MMMMMMMMM dd, yyyy</jformat><from>August 2, 1960</from><to><date><jformat>MMMMMMMMM dd, yyyy</jformat></date></to></interval>
 */
public class IntervalProcessor extends BaseTagProcessor {

    public IntervalProcessor() {
        super("interval");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String style = getAttributeOrTagValue(node, ps, "style");      // AIML 2.0
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String from = getAttributeOrTagValue(node, ps, "from");
        String to = getAttributeOrTagValue(node, ps, "to");
        if (style == null) {
            style = "years";
        }
        if (jformat == null) {
            jformat = "MMMMMMMMM dd, yyyy";
        }
        if (from == null) {
            from = "January 1, 1970";
        }
        if (to == null) {
            to = CalendarUtils.date(jformat, null, null);
        }
        String result = "unknown";
        if ("years".equals(style)) {
            result = "" + IntervalUtils.getYearsBetween(from, to, jformat);
        }
        if ("months".equals(style)) {
            result = "" + IntervalUtils.getMonthsBetween(from, to, jformat);
        }
        if ("days".equals(style)) {
            result = "" + IntervalUtils.getDaysBetween(from, to, jformat);
        }
        if ("hours".equals(style)) {
            result = "" + IntervalUtils.getHoursBetween(from, to, jformat);
        }
        return result;
    }
}
