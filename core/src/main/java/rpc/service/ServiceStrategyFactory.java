package rpc.service;

import rpc.enums.ServiceTypeEnum;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author: feiwoscun
 * @date: 2024/7/3
 * @description: Factory for creating service strategy handlers
 */
public class ServiceStrategyFactory {

    private static final List<ServiceStrategyHandler> beans = new CopyOnWriteArrayList<>();
    private final Map<ServiceTypeEnum, BaseService> serviceMap = new HashMap<>();

    // Lazy-loaded thread-safe singleton instance
    private static class SingletonHolder {
        private static final ServiceStrategyFactory INSTANCE = new ServiceStrategyFactory();
    }

    public static ServiceStrategyFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // Private constructor to prevent external instantiation
    private ServiceStrategyFactory() {

        Map<ServiceTypeEnum, BaseService> baseServiceMap = beans.stream()
                .collect(Collectors.toMap(ServiceStrategyHandler::getServiceType, service -> (BaseService) service));
        this.serviceMap.putAll(baseServiceMap);
    }

    public BaseService getCommunicationStrategy(ServiceTypeEnum serviceTypeEnum) {
        return serviceMap.get(serviceTypeEnum);
    }

    // Static method to add beans, ensuring thread safety
    public static void addBean(ServiceStrategyHandler handler) {
        beans.add(handler);
    }
}
