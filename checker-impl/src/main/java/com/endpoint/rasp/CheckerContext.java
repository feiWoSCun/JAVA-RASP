package com.endpoint.rasp;

import com.endpoint.rasp.checker.CheckChain;
import com.endpoint.rasp.checker.Checker;
import com.endpoint.rasp.checker.DefaultCheckChain;
import com.endpoint.rasp.checker.GenericChecker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: feiwoscun
 * @date: 2024/7/30
 * @email: 2825097536@qq.com
 * @description:
 */
public class CheckerContext {

    private static Map<String, List<Checker>> checkContainer = new HashMap<>();


    @Deprecated
    public void addChecker(String methodName, Checker checker) {
        checkContainer.compute(methodName, (k, v) -> {
            List<Checker> checkers = checkContainer.getOrDefault(k, new ArrayList<>());
            checkers.add(checker);
            return checkers;
        });
    }

    public synchronized static CheckChain getCheckChain(String method, Object[] args, ClassLoader classLoader, String engineName) {
        Map<String, List<Checker>> mapCheckerByString = initContainer(classLoader);
        List<Checker> checkers = mapCheckerByString.get(method);
        return new DefaultCheckChain(method, checkers.toArray(new Checker[0]), args, engineName);
    }

    /**
     * 初始化容器，从rules.json读取需要hook的类及回调
     *
     * @param classLoader
     * @return
     */
    private static synchronized Map<String, List<Checker>> initContainer(ClassLoader classLoader) {
        ServiceLoader<RuleProvider> ruleProviders = loadService(classLoader);
        Map<String, List<Checker>> mapCheckerByString = mapServiceListToMap(ruleProviders);
        checkContainer = mapCheckerByString;
        return mapCheckerByString;
    }

    /**
     * 服务发现
     *
     * @param classLoader
     * @return
     */
    private static ServiceLoader<RuleProvider> loadService(ClassLoader classLoader) {
        return RuleProviderServiceLoader.loadService(classLoader);
    }

    /**
     * @param serviceLoader
     * @return
     */
    private static Map<String, List<Checker>> mapServiceListToMap(ServiceLoader<RuleProvider> serviceLoader) {
        final List<Rule> ruleList = new ArrayList<>();
        serviceLoader.iterator().forEachRemaining(t -> t.loadRules(ruleList::addAll));

        return ruleList.stream()
                .map(t -> (Checker) new GenericChecker(t.getId(), t))
                .collect(Collectors.groupingBy(Checker::getMethods));

    }

    /**
     * 通过这个方法拿到所有需要hook的方法名
     *
     * @return 需要hook的方法名
     */
    public static Set<String> getCheckContainer() {
        if (checkContainer == null || checkContainer.isEmpty()) {
            initContainer(CheckerContext.class.getClassLoader());
        }

        return checkContainer.keySet();
    }

    /**
     * 通过这个方法拿到所有需要hook的方法名
     *
     * @param classLoader 服务发现所需要的类加载器
     * @return
     */
    public static Set<String> getCheckContainer(ClassLoader classLoader) {
        if (checkContainer == null || checkContainer.isEmpty()) {
            initContainer(classLoader);
        }

        return checkContainer.keySet();
    }
}
