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
import org.goldrenard.jb.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class Tuple extends HashMap<String, String> {

    private static final Logger log = LoggerFactory.getLogger(Tuple.class);

    private static AtomicLong index = new AtomicLong();
    private Set<String> visibleVars = new HashSet<>();
    private String name;

    protected Tuple(Set<String> varSet, Set<String> visibleVars, Tuple tuple) {
        if (visibleVars != null) {
            this.visibleVars.addAll(visibleVars);
        }
        if (varSet == null && tuple != null) {
            for (String key : tuple.keySet()) {
                put(key, tuple.get(key));
            }
            this.visibleVars.addAll(tuple.visibleVars);
        }
        if (varSet != null) {
            for (String key : varSet) {
                put(key, Constants.unbound_variable);
            }
        }
        this.name = "tuple" + index.incrementAndGet();
    }

    public Tuple(Tuple tuple) {
        this(null, null, tuple);
    }

    public Tuple(Set<String> varSet, Set<String> visibleVars) {
        this(varSet, visibleVars, null);
    }

    public Set<String> getVars() {
        return keySet();
    }

    public String getValue(String var) {
        String result = get(var);
        if (result == null) {
            return Constants.default_get;
        }
        return result;
    }

    public void bind(String var, String value) {
        if (get(var) != null && !get(var).equals(Constants.unbound_variable)) {
            log.warn("{} already bound to {}", var, get(var));
        } else {
            put(var, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !Tuple.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        Tuple tuple = (Tuple) o;
        if (visibleVars.size() != tuple.visibleVars.size()) {
            return false;
        }
        for (String x : visibleVars) {
            if (!tuple.visibleVars.contains(x)) {
                return false;
            } else if (get(x) != null && !get(x).equals(tuple.get(x))) {
                return false;
            }
        }
        return !values().contains(Constants.unbound_variable)
                && !tuple.values().contains(Constants.unbound_variable);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (String x : visibleVars) {
            result = 31 * result + x.hashCode();
            if (get(x) != null) {
                result = 31 * result + get(x).hashCode();
            }
        }
        return result;
    }
}
