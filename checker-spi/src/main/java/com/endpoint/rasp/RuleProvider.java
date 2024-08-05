package com.endpoint.rasp;

import java.util.List;
import java.util.function.Function;


/**
 * @author feiwoscun
 */
public interface RuleProvider {
    void loadRules();

    List<Rule> getRules();

    <R> void loadRules(Function<List<Rule>, R> function);

}
