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
import org.goldrenard.jb.parser.base.CollectionResource;
import org.goldrenard.jb.utils.Utilities;

public class PronounsResource extends CollectionResource<String> {

    private final static String PRONOUNS_FILE = "pronouns.txt";

    @Override
    public int read(String path) {
        Utilities
                .readFileLines(path + "/" + PRONOUNS_FILE)
                .stream()
                .filter(StringUtils::isNotEmpty).forEach(this::add);
        return size();
    }
}
