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
package org.goldrenard.jb.parser.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public interface ParsedResource<T> {

    Logger log = LoggerFactory.getLogger(ParsedResource.class);

    int read(String path);

    default void write(T resource) {
        // no default implementation
    }

    default void write(Collection<T> resources) {
        for (T resource : resources) {
            try {
                write(resource);
            } catch (Exception e) {
                log.error("Could not write resource {}", resource);
            }
        }
    }
}
