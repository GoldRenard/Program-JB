package org.goldrenard.jb.process;

import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.CalendarUtils;
import org.goldrenard.jb.utils.IntervalUtils;
import org.w3c.dom.Node;

/**
 * <interval><style>years</style></style><jformat>MMMMMMMMM dd, yyyy</jformat><from>August 2, 1960</from><to><date><jformat>MMMMMMMMM dd, yyyy</jformat></date></to></interval>
 */
public class IntervalProcessor extends BaseNodeProcessor {

    public IntervalProcessor(AIMLProcessor processor) {
        super(processor, "interval");
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
