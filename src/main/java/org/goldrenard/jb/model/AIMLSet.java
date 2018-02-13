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
package org.goldrenard.jb.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.core.Bot;
import org.goldrenard.jb.core.Sraix;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * implements AIML Sets
 */
@Getter
@Setter
@ToString
public class AIMLSet extends HashSet<String> implements NamedEntity {

    private static final Pattern DIGITS_PATTERN = Pattern.compile("[0-9]+");

    private final String name;

    private final Bot bot;

    private int maxLength = 1; // there are no empty sets

    private String host; // for external sets
    private String botId; // for external sets
    private boolean external = false;
    private Set<String> inCache = new HashSet<>();
    private Set<String> outCache = new HashSet<>();

    /**
     * constructor
     *
     * @param name name of set
     */
    public AIMLSet(String name, Bot bot) {
        super();
        this.bot = bot;
        this.name = name.toLowerCase();
        if (name.equals(Constants.natural_number_set_name)) {
            maxLength = 1;
        }
    }

    public boolean contains(String s) {
        if (external && bot.getConfiguration().isEnableExternalSets()) {
            if (inCache.contains(s)) {
                return true;
            }
            if (outCache.contains(s)) {
                return false;
            }
            String[] split = s.split(" ");
            if (split.length > maxLength) {
                return false;
            }
            String query = Constants.set_member_string + name.toUpperCase() + " " + s;
            String response = Sraix.sraix(null, bot, query, "false", null, host, botId, null, "0");
            if ("true".equals(response)) {
                inCache.add(s);
                return true;
            } else {
                outCache.add(s);
                return false;
            }
        } else if (name.equals(Constants.natural_number_set_name)) {
            return DIGITS_PATTERN.matcher(s).matches();
        }
        return super.contains(s);
    }
}
