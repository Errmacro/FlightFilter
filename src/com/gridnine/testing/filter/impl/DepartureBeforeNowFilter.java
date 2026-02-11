package com.gridnine.testing.filter.impl;

import com.gridnine.testing.Flight;
import com.gridnine.testing.filter.FlightFilter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DepartureBeforeNowFilter implements FlightFilter {

    private final Clock clock;

    public DepartureBeforeNowFilter() {
        this(Clock.systemDefaultZone());
    }

    public DepartureBeforeNowFilter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
    }

    @Override
    public boolean test(Flight flight) {
        if (flight == null) {
            return false;
        }
        return flight.getSegments().stream()
                .allMatch(segment -> !segment.getDepartureDate().isBefore(LocalDateTime.now(clock)));
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

    @Override
    public String toString() {
        return "DepartureBeforeNowFilter{clock=" + clock + "}";
    }
}
