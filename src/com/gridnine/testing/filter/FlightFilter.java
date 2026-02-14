package com.gridnine.testing.filter;

import com.gridnine.testing.Flight;

import java.util.List;
import java.util.function.Predicate;

/**
 * Базовый интерфейс для всех фильтров перелётов.
 * Поддерживает композицию фильтров (AND, OR, NOT).
 */
public interface FlightFilter extends Predicate<Flight> {

    /**
     * Применяет фильтр к списку перелётов.
     * @param flights список перелётов (может быть null)
     * @return отфильтрованный список (всегда не null)
     */
    List<Flight> apply(List<Flight> flights);

    /**
     * Композиция: AND (все фильтры должны пройти)
     */
    static FlightFilter allOf(FlightFilter... filters) {
        return new CompositeFlightFilter(CompositeFlightFilter.Operator.AND, filters);
    }

    /**
     * Композиция: OR (хотя бы один фильтр должен пройти)
     */
    static FlightFilter anyOf(FlightFilter... filters) {
        return new CompositeFlightFilter(CompositeFlightFilter.Operator.OR, filters);
    }

    /**
     * Композиция: NOT (отрицание фильтра)
     */
    static FlightFilter not(FlightFilter filter) {
        return new NotFlightFilter(filter);
    }
}
