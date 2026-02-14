package com.gridnine.testing.filter.impl;

import com.gridnine.testing.Flight;
import com.gridnine.testing.Segment;
import com.gridnine.testing.filter.FlightFilter;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Фильтр, исключающий перелёты, у которых общее время на земле
 * превышает заданный лимит (по умолчанию 120 минут).
 *
 * <p>Время на земле считается как сумма интервалов между прилётом
 * текущего сегмента и вылетом следующего за ним.
 *
 * <p>Перелёты с одним сегментом всегда проходят фильтр,
 * так как не имеют времени на земле.
 */
public class GroundTimeExceedsTwoHoursFilter implements FlightFilter {
    private final int maxGroundMinutes;
    public static final int DEFAULT_MAX_MINUTES = 120;

    public GroundTimeExceedsTwoHoursFilter() {
        this(DEFAULT_MAX_MINUTES);
    }

    public GroundTimeExceedsTwoHoursFilter(int maxGroundMinutes) {
        if (maxGroundMinutes < 0) {
            throw new IllegalArgumentException("Max ground minutes cannot be negative");
        }
        this.maxGroundMinutes = maxGroundMinutes;
    }

    @Override
    public boolean test(Flight flight) {
        if (flight == null) {
            return false;
        }

        List<Segment> segments = flight.getSegments();
        if (segments.size() < 2) {
            return true;
        }

        long totalGroundMinutes = calculateTotalGroundMinutes(segments);
        return totalGroundMinutes <= maxGroundMinutes;
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

    private long calculateTotalGroundMinutes(List<Segment> segments) {
        long total = 0;
        for (int i = 0; i < segments.size() - 1; i++) {
            Segment current = segments.get(i);
            Segment next = segments.get(i + 1);
            total += Duration.between(current.getArrivalDate(), next.getDepartureDate()).toMinutes();
        }
        return total;
    }

    public int getMaxGroundMinutes() {
        return maxGroundMinutes;
    }

    @Override
    public String toString() {
        return "GroundTimeExceedsFilter{maxGroundMinutes=" + maxGroundMinutes + "}";
    }
}
