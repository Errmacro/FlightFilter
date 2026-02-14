package com.gridnine.testing.filter.impl;

import com.gridnine.testing.Flight;
import com.gridnine.testing.filter.FlightFilter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Фильтр, исключающий перелёты, у которых хотя бы один сегмент имеет вылет в прошлом.
 *
 * <p>Перелёт считается валидным, если дата вылета каждого сегмента НЕ раньше
 * текущего момента времени (now). Сегменты с вылетом ровно в момент now считаются валидными.
 *
 * <p>Фильтр поддерживает инъекцию Clock для возможности фиксации времени в тестах.
 */
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
