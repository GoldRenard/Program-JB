package org.goldrenard.jb.model;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Substitution {

    private Pattern pattern;

    private String substitution;
}
