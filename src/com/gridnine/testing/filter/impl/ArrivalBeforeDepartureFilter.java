package com.gridnine.testing.filter.impl;

import com.gridnine.testing.Flight;
import com.gridnine.testing.filter.FlightFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Фильтр, исключающий перелёты с некорректными сегментами,
 * где дата прилёта раньше даты вылета.
 *
 * <p>Перелёт считается валидным, если в каждом его сегменте
 * прилёт происходит после вылета (или одновременно - в зависимости от требования).
 */
public class ArrivalBeforeDepartureFilter implements FlightFilter {

    @Override
    public boolean test(Flight flight) {
        if (flight == null) {
            return false;
        }
        return flight.getSegments().stream()
                .allMatch(segment -> segment.getArrivalDate().isAfter(segment.getDepartureDate()));
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
        return "ArrivalBeforeDepartureFilter{}";
    }
}
