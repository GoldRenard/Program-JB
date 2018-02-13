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
package org.goldrenard.jb.core;

import lombok.Getter;
import lombok.Setter;
import org.goldrenard.jb.configuration.Constants;
import org.goldrenard.jb.model.Clause;
import org.goldrenard.jb.model.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class TripleStore {

    private static final Logger log = LoggerFactory.getLogger(TripleStore.class);

    private int idCnt = 0;
    private String name = "unknown";
    private Bot bot;
    private Map<String, Triple> idTriple = new HashMap<>();
    private Map<String, String> tripleStringId = new HashMap<>();
    private Map<String, Set<String>> subjectTriples = new HashMap<>();
    private Map<String, Set<String>> predicateTriples = new HashMap<>();
    private Map<String, Set<String>> objectTriples = new HashMap<>();

    private Map<String, Tuple> tupleMap = new ConcurrentHashMap<>();

    public TripleStore(String name, Bot bot) {
        this.name = name;
        this.bot = bot;
    }

    @Getter
    @Setter
    public class Triple {
        private String id;
        private String subject;
        private String predicate;
        private String object;

        public Triple(String s, String p, String o) {
            Bot bot = TripleStore.this.bot;
            if (bot != null) {
                s = bot.getPreProcessor().normalize(s);
                p = bot.getPreProcessor().normalize(p);
                o = bot.getPreProcessor().normalize(o);
            }
            if (s != null && p != null && o != null) {
                subject = s;
                predicate = p;
                object = o;
                id = name + idCnt++;
            }
        }
    }

    public String mapTriple(Triple triple) {
        String id = triple.id;
        idTriple.put(id, triple);
        String subject = triple.subject.toUpperCase();
        String predicate = triple.predicate.toUpperCase();
        String object = triple.object.toUpperCase();

        String tripleString = subject + ":" + predicate + ":" + object;
        tripleString = tripleString.toUpperCase();

        if (tripleStringId.keySet().contains(tripleString)) {
            return tripleStringId.get(tripleString); // triple already exists
        } else {
            tripleStringId.put(tripleString, id);

            Set<String> existingTriples = subjectTriples.getOrDefault(subject, new HashSet<>());
            existingTriples.add(id);
            subjectTriples.put(subject, existingTriples);

            existingTriples = predicateTriples.getOrDefault(predicate, new HashSet<>());
            existingTriples.add(id);
            predicateTriples.put(predicate, existingTriples);

            existingTriples = objectTriples.getOrDefault(object, new HashSet<>());
            existingTriples.add(id);
            objectTriples.put(object, existingTriples);
            return id;
        }
    }

    public String unMapTriple(Triple triple) {
        String subject = triple.subject.toUpperCase();
        String predicate = triple.predicate.toUpperCase();
        String object = triple.object.toUpperCase();

        String tripleString = subject + ":" + predicate + ":" + object;

        log.debug("unMapTriple {}", tripleString);
        tripleString = tripleString.toUpperCase();
        triple = idTriple.get(tripleStringId.get(tripleString));
        log.debug("unMapTriple {}", triple);
        if (triple != null) {
            String id = triple.id;
            idTriple.remove(id);
            tripleStringId.remove(tripleString);

            Set<String> existingTriples = subjectTriples.getOrDefault(subject, new HashSet<>());
            existingTriples.remove(id);
            subjectTriples.put(subject, existingTriples);

            existingTriples = predicateTriples.getOrDefault(predicate, new HashSet<>());
            existingTriples.remove(id);
            predicateTriples.put(predicate, existingTriples);

            existingTriples = objectTriples.getOrDefault(object, new HashSet<>());
            existingTriples.remove(id);
            objectTriples.put(object, existingTriples);
            return id;
        }
        return Constants.undefined_triple;
    }

    public Set<String> allTriples() {
        return new HashSet<>(idTriple.keySet());
    }

    public String addTriple(String subject, String predicate, String object) {
        if (subject == null || predicate == null || object == null) {
            return Constants.undefined_triple;
        }
        Triple triple = new Triple(subject, predicate, object);
        return mapTriple(triple);
    }

    public String deleteTriple(String subject, String predicate, String object) {
        if (subject == null || predicate == null || object == null) {
            return Constants.undefined_triple;
        }
        if (log.isTraceEnabled()) {
            log.trace("Deleting {}:{}:{}", subject, predicate, object);
        }
        Triple triple = new Triple(subject, predicate, object);
        return unMapTriple(triple);
    }

    public void printTriples() {
        for (String x : idTriple.keySet()) {
            Triple triple = idTriple.get(x);
            log.info("{}:{}:{}:{}", x, triple.subject, triple.predicate, triple.object);
        }
    }

    private Set<String> emptySet() {
        return new HashSet<>();
    }

    public Set<String> getTriples(String s, String p, String o) {
        Set<String> subjectSet;
        Set<String> predicateSet;
        Set<String> objectSet;
        Set<String> resultSet;
        if (log.isTraceEnabled()) {
            log.trace("TripleStore: getTriples [{}] {}:{}:{}", idTriple.size(), s, p, o);
        }
        if (s == null || s.startsWith("?")) {
            subjectSet = allTriples();
        } else {
            subjectSet = subjectTriples.getOrDefault(s.toUpperCase(), emptySet());
        }

        if (p == null || p.startsWith("?")) {
            predicateSet = allTriples();
        } else {
            predicateSet = predicateTriples.getOrDefault(p.toUpperCase(), emptySet());
        }

        if (o == null || o.startsWith("?")) {
            objectSet = allTriples();
        } else {
            objectSet = objectTriples.getOrDefault(o.toUpperCase(), emptySet());
        }

        resultSet = new HashSet<>(subjectSet);
        resultSet.retainAll(predicateSet);
        resultSet.retainAll(objectSet);
        return resultSet;
    }

    public Set<String> getSubjects(Set<String> triples) {
        HashSet<String> resultSet = new HashSet<>();
        for (String id : triples) {
            Triple triple = idTriple.get(id);
            resultSet.add(triple.subject);
        }
        return resultSet;
    }

    public Set<String> getPredicates(Set<String> triples) {
        HashSet<String> resultSet = new HashSet<>();
        for (String id : triples) {
            Triple triple = idTriple.get(id);
            resultSet.add(triple.predicate);
        }
        return resultSet;
    }

    public Set<String> getObjects(Set<String> triples) {
        Set<String> resultSet = new HashSet<>();
        for (String id : triples) {
            Triple triple = idTriple.get(id);
            resultSet.add(triple.object);
        }
        return resultSet;
    }

    public String getSubject(String id) {
        if (idTriple.containsKey(id)) {
            return idTriple.get(id).subject;
        }
        return "Unknown subject";
    }

    public String getPredicate(String id) {
        if (idTriple.containsKey(id)) {
            return idTriple.get(id).predicate;
        }
        return "Unknown predicate";
    }

    public String getObject(String id) {
        if (idTriple.containsKey(id)) {
            return idTriple.get(id).object;
        }
        return "Unknown object";
    }

    public String stringTriple(String id) {
        Triple triple = idTriple.get(id);
        return id + " " + triple.subject + " " + triple.predicate + " " + triple.object;
    }

    public void printAllTriples() {
        for (String id : idTriple.keySet()) {
            log.info("{}", stringTriple(id));
        }
    }

    public Set<Tuple> select(Set<String> vars, Set<String> visibleVars, List<Clause> clauses) {
        Set<Tuple> result = new HashSet<>();
        try {
            Tuple tuple = storeTuple(new Tuple(vars, visibleVars));
            result = selectFromRemainingClauses(tuple, clauses);
        } catch (Exception e) {
            log.error("Error", e);
        }
        return result;
    }

    public Clause adjustClause(Tuple tuple, Clause clause) {
        Set vars = tuple.getVars();
        String subj = clause.getSubj();
        String pred = clause.getPred();
        String obj = clause.getObj();
        Clause newClause = new Clause(clause);
        if (vars.contains(subj)) {
            String value = tuple.getValue(subj);
            if (!value.equals(Constants.unbound_variable)) {
                newClause.setSubj(value);
            }
        }
        if (vars.contains(pred)) {
            String value = tuple.getValue(pred);
            if (!value.equals(Constants.unbound_variable)) {
                newClause.setPred(value);
            }
        }
        if (vars.contains(obj)) {
            String value = tuple.getValue(obj);
            if (!value.equals(Constants.unbound_variable)) {
                newClause.setObj(value);
            }
        }
        return newClause;

    }

    public Tuple bindTuple(Tuple partial, String triple, Clause clause) {
        Tuple tuple = storeTuple(new Tuple(partial));
        if (clause.getSubj().startsWith("?")) tuple.bind(clause.getSubj(), getSubject(triple));
        if (clause.getPred().startsWith("?")) tuple.bind(clause.getPred(), getPredicate(triple));
        if (clause.getObj().startsWith("?")) tuple.bind(clause.getObj(), getObject(triple));
        return tuple;
    }

    public Set<Tuple> selectFromSingleClause(Tuple partial, Clause clause, Boolean affirm) {
        Set<Tuple> result = new HashSet<>();
        Set<String> triples = getTriples(clause.getSubj(), clause.getPred(), clause.getObj());
        if (affirm) {
            for (String triple : triples) {
                Tuple tuple = bindTuple(partial, triple, clause);
                result.add(tuple);
            }
        } else if (triples.size() == 0) {
            result.add(partial);
        }
        return result;
    }

    public Set<Tuple> selectFromRemainingClauses(Tuple partial, List<Clause> clauses) {
        Set<Tuple> result = new HashSet<>();
        Clause clause = clauses.get(0);
        clause = adjustClause(partial, clause);
        Set<Tuple> tuples = selectFromSingleClause(partial, clause, clause.getAffirm());
        if (clauses.size() > 1) {
            List<Clause> remainingClauses = new ArrayList<>(clauses);
            remainingClauses.remove(0);
            for (Tuple tuple : tuples) {
                result.addAll(selectFromRemainingClauses(tuple, remainingClauses));
            }
        } else result = tuples;
        return result;
    }

    public Tuple storeTuple(Tuple tuple) {
        tupleMap.put(tuple.getName(), tuple);
        return tuple;
    }

    public String tupleGet(String tupleName, String varName) {
        Tuple tuple = tupleMap.get(tupleName);
        return tuple == null ? Constants.default_get : tuple.getValue(varName);
    }
}
