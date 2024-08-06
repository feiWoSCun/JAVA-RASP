package com.endpoint.rasp;

import java.util.List;
import java.util.function.Function;


/**
 * @author: feiwoscun
 * @date: 2024/7/31
 * @email: 2825097536@qq.com
 * @description:
 */
public interface RuleProvider {
    void loadRules();

    List<Rule> getRules();

    <R> void loadRules(Function<List<Rule>, R> function);

}
