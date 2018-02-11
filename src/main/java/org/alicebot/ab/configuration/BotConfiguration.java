package org.alicebot.ab.configuration;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

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
    private String programName = "Program AB 0.0.6.26 beta -- AI Foundation Reference AIML 2.1 implementation";
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
