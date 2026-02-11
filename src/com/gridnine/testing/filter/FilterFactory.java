package com.gridnine.testing.filter;

import com.gridnine.testing.filter.impl.DepartureBeforeNowFilter;
import com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter;
import com.gridnine.testing.filter.impl.ArrivalBeforeDepartureFilter;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Фабрика для динамического создания фильтров.
 * Позволяет регистрировать новые фильтры без изменения существующего кода.
 */
public class FilterFactory {

    private static final Map<FilterType, Supplier<FlightFilter>> FILTER_REGISTRY = new HashMap<>();

    static {
        register(FilterType.DEPARTURE_BEFORE_NOW, DepartureBeforeNowFilter::new);
        register(FilterType.ARRIVAL_BEFORE_DEPARTURE, ArrivalBeforeDepartureFilter::new);
        register(FilterType.GROUND_TIME_EXCEEDS, GroundTimeExceedsTwoHoursFilter::new);
    }

    /**
     * Регистрация нового типа фильтра.
     */
    public static void register(FilterType type, Supplier<FlightFilter> supplier) {
        FILTER_REGISTRY.put(type, supplier);
    }

    /**
     * Создание фильтра по типу.
     */
    public static FlightFilter createFilter(FilterType type) {
        Supplier<FlightFilter> supplier = FILTER_REGISTRY.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown filter type: " + type);
        }
        return supplier.get();
    }

    /**
     * Создание фильтра с параметрами.
     */
    public static FlightFilter createFilter(FilterType type, Object... params) {
        if (type == FilterType.GROUND_TIME_EXCEEDS && params.length > 0) {
            if (params[0] instanceof Integer) {
                return new GroundTimeExceedsTwoHoursFilter((Integer) params[0]);
            }
        }
        return createFilter(type);
    }

    /**
     * Создание фильтра с кастомным Clock (для тестирования).
     */
    public static FlightFilter createFilter(FilterType type, Clock clock) {
        if (type == FilterType.DEPARTURE_BEFORE_NOW) {
            return new DepartureBeforeNowFilter(clock);
        }
        return createFilter(type);
    }

    /**
     * Получение всех зарегистрированных типов фильтров.
     */
    public static Map<FilterType, String> getAvailableFilters() {
        Map<FilterType, String> available = new HashMap<>();
        FILTER_REGISTRY.keySet().forEach(type -> available.put(type, type.getDescription()));
        return available;
    }
}
