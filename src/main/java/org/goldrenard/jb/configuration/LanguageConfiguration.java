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
