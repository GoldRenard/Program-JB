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
package org.goldrenard.jb.configuration;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.goldrenard.jb.tags.base.AIMLTagProcessor;

import java.util.List;

@Getter
@Setter
@Builder
public class BotConfiguration {

    private static final String DEFAULT_NAME = "alice2";

    private static final String DEFAULT_PATH = "/";

    private static final String DEFAULT_ACTION = "auto";

    private String name;
    private String path;
    private String action;

    @Builder.Default
    private boolean enableExternalSets = true;
    @Builder.Default
    private boolean enableExternalMaps = true;
    @Builder.Default
    private boolean enableSystemTag = false;
    @Builder.Default
    private boolean jpTokenize = false;
    @Builder.Default
    private boolean fixExcelCsv = true;
    @Builder.Default
    private boolean enableNetworkConnection = true;
    @Builder.Default
    private boolean qaTestMode = false;
    @Builder.Default
    private boolean makeVerbsSetsMaps = false;
    @Builder.Default
    private boolean graphShortCuts = false;

    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private int maxHistory = 32;
    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private int repetitionCount = 2;

    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private int maxStars = 1000;
    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private int maxGraphHeight = 100000;

    @Setter(AccessLevel.PROTECTED)
    @Builder.Default
    private int maxSubstitutions = 10000;
    @Builder.Default
    private int maxRecursionDepth = 765; // assuming java -Xmx512M
    @Builder.Default
    private int maxRecursionCount = 2048;
    @Builder.Default
    private int maxLoops = 10000;

    @Builder.Default
    private String programName = "Program JB -- AI Foundation Reference AIML 2.1 implementation";
    @Builder.Default
    private String defaultLanguage = "EN";
    @Builder.Default
    private String aimlifSplitChar = ",";
    @Builder.Default
    private String aimlifSplitCharName = "\\#Comma";
    @Builder.Default
    private String aimlifFileSuffix = ".csv";
    @Builder.Default
    private String pannousApiKey = "guest";
    @Builder.Default
    private String pannousLogin = "test-user";
    @Builder.Default
    private LanguageConfiguration language = LanguageConfiguration.builder().build();

    // extensions
    @Singular
    private List<AIMLTagProcessor> tagProcessors;

    public String getName() {
        return StringUtils.isNotBlank(name) ? name : DEFAULT_NAME;
    }

    public String getAction() {
        return StringUtils.isNotBlank(action) ? action : DEFAULT_ACTION;
    }

    public String getPath() {
        String path = this.path;
        if (StringUtils.isBlank(path)) {
            path = System.getProperty("user.dir");
        }
        if (StringUtils.isBlank(path)) {
            path = DEFAULT_PATH;
        }
        return path;
    }
}
