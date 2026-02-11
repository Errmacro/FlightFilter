package com.gridnine.testing.filter;

import com.gridnine.testing.Flight;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Фильтр-негатор.
 */
public class NotFlightFilter implements FlightFilter {

    private final FlightFilter original;

    public NotFlightFilter(FlightFilter original) {
        this.original = Objects.requireNonNull(original, "Original filter cannot be null");
    }

    @Override
    public boolean test(Flight flight) {
        return flight != null && !original.test(flight);
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
