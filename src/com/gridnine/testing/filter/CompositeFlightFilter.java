package com.gridnine.testing.filter;

import com.gridnine.testing.Flight;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Композитный фильтр для комбинирования нескольких фильтров с логическими операциями.
 */
public class CompositeFlightFilter implements FlightFilter {

    public enum Operator {
        AND, OR
    }

    private final List<FlightFilter> filters;
    private final Operator operator;

    public CompositeFlightFilter(Operator operator, FlightFilter... filters) {
        this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
        this.filters = Arrays.stream(filters)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean test(Flight flight) {
        if (flight == null || filters.isEmpty()) {
            return false;
        }

        if (operator == Operator.AND) {
            return filters.stream().allMatch(f -> f.test(flight));
        } else {
            return filters.stream().anyMatch(f -> f.test(flight));
        }
    }

    @Override
    public List<Flight> apply(List<Flight> flights) {
        if (flights == null || flights.isEmpty()) {
            return List.of();
        }

        return flights.stream()
                .filter(Objects::nonNull)
                .filter(this)
                .collect(Collectors.toList());
    }
}
