package org.goldrenard.jb.process;

import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
import org.goldrenard.jb.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * return the client ID.
 * implements {@code <id/>}
 */
public class JavaScriptProcessor extends BaseNodeProcessor {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptProcessor.class);

    public JavaScriptProcessor(AIMLProcessor processor) {
        super(processor, "javascript");
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
