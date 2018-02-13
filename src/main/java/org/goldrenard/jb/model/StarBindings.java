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

/**
 * structure to hold binding of wildcards in input pattern, that pattern and topicpattern
 */
@Getter
@Setter
public class StarBindings {
    private Stars inputStars;
    private Stars thatStars;
    private Stars topicStars;

    /**
     * Constructor  -- this class has public members
     */
    public StarBindings() {
        inputStars = new Stars();
        thatStars = new Stars();
        topicStars = new Stars();
    }
}
