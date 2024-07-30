package com.endpoint.rasp;

import com.endpoint.rasp.checker.Checker;

import java.util.*;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class CheckerContext {
    private ClassLoader classLoader;
    public static Map<String, List<Checker>> checkContainer = new HashMap<>();


    public void addChecker(String methodName, Checker checker) {
        checkContainer.compute(methodName, (k, v) -> {
            List<Checker> checkers = checkContainer.getOrDefault(k, new ArrayList<>());
            checkers.add(checker);
            return checkers;
        });
    }

    public ServiceLoader<Checker> loadService() {
        return CheckerServiceLoader.loadService(classLoader);
    }

    public Map<String, List<Checker>> mapServiceListToMap(ServiceLoader<Checker> serviceLoader) {
        final List<Checker> checkerLists = new ArrayList<>();
        final Set<String> methods = new HashSet<>();


        Iterator<Checker> checkers = serviceLoader.iterator();
        checkers.forEachRemaining(checkerLists::add);

        checkerLists.forEach(t -> {
            Set<String> tMethods = t.getMethods();
            tMethods.forEach(m -> {
                this.addChecker(m, t);
            });
        });

    }
}
