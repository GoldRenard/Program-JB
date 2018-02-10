package org.alicebot.ab;


import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
                put(key, MagicStrings.unbound_variable);
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
            return MagicStrings.default_get;
        }
        return result;
    }

    public void bind(String var, String value) {
        if (get(var) != null && !get(var).equals(MagicStrings.unbound_variable)) {
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
        return !values().contains(MagicStrings.unbound_variable)
                && !tuple.values().contains(MagicStrings.unbound_variable);
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
