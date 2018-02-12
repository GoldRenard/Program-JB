package org.goldrenard.jb.configuration;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LanguageConfiguration {

    @Builder.Default
    private String defaultResponse = "I have no answer for that.";
    @Builder.Default
    private String errorResponse = "Something is wrong with my brain.";
    @Builder.Default
    private String scheduleError = "I'm unable to schedule that event.";
    @Builder.Default
    private String systemFailed = "Failed to execute system command.";
    @Builder.Default
    private String templateFailed = "Template failed.";
    @Builder.Default
    private String tooMuchRecursion = "Too much recursion in AIML";
    @Builder.Default
    private String tooMuchLooping = "Too much looping in AIML";

}
