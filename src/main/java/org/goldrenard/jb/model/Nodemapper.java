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

import java.util.List;
import java.util.Map;

/**
 * Nodemapper data structure.  In order to minimize memory overhead this class has no methods.
 * Operations on Nodemapper objects are performed by NodemapperOperator class
 */
@Getter
@Setter
public class Nodemapper {

    private Category category = null;
    private int height;
    private StarBindings starBindings = null;
    private Map<String, Nodemapper> map = null;
    private String key = null;
    private Nodemapper value = null;
    private boolean shortCut = false;
    private List<String> sets;

    public Nodemapper(int height) {
        this.height = height;
    }
}


