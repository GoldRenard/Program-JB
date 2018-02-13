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
package org.goldrenard.jb.parser;

import org.apache.commons.lang3.StringUtils;
import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.model.Substitution;
import org.goldrenard.jb.parser.base.CollectionResource;
import org.goldrenard.jb.utils.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubstitutionResource extends CollectionResource<Substitution> {

    private final static Pattern ENTRY_PATTERN = Pattern.compile("\"(.*?)\",\"(.*?)\"", Pattern.DOTALL);

    private final Bot bot;

    public SubstitutionResource(Bot bot) {
        this.bot = bot;
    }

    @Override
    public int read(String filePath) {
        Utilities.readFileLines(filePath)
                .stream()
                .filter(StringUtils::isNotEmpty)
                .forEach(e -> {
                    if (size() < bot.getConfiguration().getMaxSubstitutions()) {
                        Substitution substitution = parse(e);
                        if (substitution != null) {
                            add(substitution);
                        }
                    }
                });
        return size();
    }

    private Substitution parse(String input) {
        Substitution substitution = null;
        Matcher matcher = ENTRY_PATTERN.matcher(input);
        if (matcher.find()) {
            substitution = new Substitution();
            String quotedPattern = Pattern.quote(matcher.group(1));
            substitution.setSubstitution(matcher.group(2));
            substitution.setPattern(Pattern.compile(quotedPattern, Pattern.CASE_INSENSITIVE));
        }
        return substitution;
    }
}
