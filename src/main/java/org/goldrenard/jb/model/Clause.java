package org.goldrenard.jb.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Clause {
    private String subj;
    private String pred;
    private String obj;
    private Boolean affirm;

    public Clause(String s, String p, String o) {
        this(s, p, o, true);
    }

    public Clause(String s, String p, String o, Boolean affirm) {
        subj = s;
        pred = p;
        obj = o;
        this.affirm = affirm;
    }

    public Clause(Clause clause) {
        this(clause.subj, clause.pred, clause.obj, clause.affirm);
    }
}
