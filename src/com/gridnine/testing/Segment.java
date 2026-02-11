package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Представляет атомарный сегмент перелёта - перевозку из одной точки в другую.
 * Сегмент характеризуется датой/временем вылета и датой/временем прилёта.
 * Объект Segment является неизменяемым (immutable).
 */
public class Segment {
    private final LocalDateTime departureDate;
    private final LocalDateTime arrivalDate;

    public Segment(final LocalDateTime dep, final LocalDateTime arr) {
        this.departureDate = Objects.requireNonNull(dep, "Departure date cannot be null");
        this.arrivalDate = Objects.requireNonNull(arr, "Arrival date cannot be null");
    }

    public LocalDateTime getDepartureDate() {
        return departureDate;
    }

    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return '[' + departureDate.format(fmt) + '|' + arrivalDate.format(fmt) + ']';
    }
}
