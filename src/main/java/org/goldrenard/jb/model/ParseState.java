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
import org.goldrenard.jb.core.AIMLProcessor;
import org.goldrenard.jb.core.Chat;

/**
 * ParseState is a helper class for AIMLProcessor
 */
@Getter
@Setter
public class ParseState {
    private final AIMLProcessor processor;
    private Nodemapper leaf;
    private String input;
    private String that;
    private String topic;
    private Chat chatSession;
    private int depth;
    private Predicates vars;
    private StarBindings starBindings;
    private int sraiCount;

    /**
     * Constructor - class has public members
     *
     * @param depth       depth in parse tree
     * @param chatSession client session
     * @param input       client input
     * @param that        bot's last sentence
     * @param topic       current topic
     * @param leaf        node containing the category processed
     */
    public ParseState(AIMLProcessor processor, int depth, Chat chatSession, String input, String that, String topic, Nodemapper leaf, int sraiCount) {
        this.processor = processor;
        this.chatSession = chatSession;
        this.input = input;
        this.that = that;
        this.topic = topic;
        this.leaf = leaf;
        this.depth = depth;  // to prevent runaway recursion
        this.vars = new Predicates(chatSession.getBot());
        this.starBindings = leaf.getStarBindings();
        this.sraiCount = sraiCount;
    }
}
