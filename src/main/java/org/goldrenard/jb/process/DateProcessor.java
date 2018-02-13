package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.CalendarUtils;
import org.w3c.dom.Node;

/**
 * implements formatted date tag <date jformat="format"/> and <date format="format"/>
 */
public class DateProcessor extends BaseNodeProcessor {

    public DateProcessor(AIMLProcessor processor) {
        super(processor, "date");
    }

    @Override
    public String eval(Node node, ParseState ps) {
        String jformat = getAttributeOrTagValue(node, ps, "jformat");      // AIML 2.0
        String locale = getAttributeOrTagValue(node, ps, "locale");
        String timezone = getAttributeOrTagValue(node, ps, "timezone");
        return CalendarUtils.date(jformat, locale, timezone);
    }
}
