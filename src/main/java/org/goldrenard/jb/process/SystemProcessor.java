package org.goldrenard.jb.process;

import org.goldrenard.jb.Bot;
import org.goldrenard.jb.model.ParseState;
import org.goldrenard.jb.process.base.AIMLProcessor;
import org.goldrenard.jb.process.base.BaseNodeProcessor;
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
public class SystemProcessor extends BaseNodeProcessor {

    public SystemProcessor(AIMLProcessor processor) {
        super(processor, "system");
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
