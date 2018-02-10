/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/
package org.alicebot.ab.model;

import lombok.Getter;
import lombok.Setter;
import org.alicebot.ab.configuration.MagicNumbers;

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
    private int height = MagicNumbers.max_graph_height;
    private StarBindings starBindings = null;
    private Map<String, Nodemapper> map = null;
    private String key = null;
    private Nodemapper value = null;
    private boolean shortCut = false;
    private List<String> sets;

}


