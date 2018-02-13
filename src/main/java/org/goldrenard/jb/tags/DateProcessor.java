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
import org.w3c.dom.Node;

/**
 * implements formatted date tag <date jformat="format"/> and <date format="format"/>
 */
public class DateProcessor extends BaseTagProcessor {

    public DateProcessor() {
        super("date");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String locale = getAttributeOrTagValue(node, ps, "locale");
        String timezone = getAttributeOrTagValue(node, ps, "timezone");
        return CalendarUtils.date(jformat, locale, timezone);
    }
}
