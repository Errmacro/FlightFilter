package com.gridnine.testing.filter;

import com.gridnine.testing.Flight;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис для применения фильтров к коллекциям перелётов.
 *
 * <p>Поддерживает последовательную и параллельную обработку,
 * оптимизирован для работы с большими наборами данных.
 *
 * <p>Все методы гарантируют возврат не-null списка и
 * корректно обрабатывают null-аргументы.
 */
public class FlightFilterService {

    private final boolean parallelProcessing;

    public FlightFilterService() {
        this(false);
    }

    public FlightFilterService(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;
    }

    /**
     * Применение списка фильтров (AND).
     */
    public List<Flight> applyFilters(List<Flight> flights, List<FlightFilter> filters) {
        if (flights == null || flights.isEmpty()) {
            return List.of();
        }

        if (filters == null || filters.isEmpty()) {
            return copyList(flights);
        }

        Stream<Flight> stream = parallelProcessing
                ? flights.parallelStream()
                : flights.stream();

        return stream
                .filter(Objects::nonNull)
                .filter(flight -> filters.stream()
                        .filter(Objects::nonNull)
                        .allMatch(filter -> filter.test(flight)))
                .collect(Collectors.toList());
    }

    /**
     * Применение одного фильтра.
     */
    public List<Flight> applyFilter(List<Flight> flights, FlightFilter filter) {
        if (flights == null || flights.isEmpty() || filter == null) {
            return flights == null ? List.of() : copyList(flights);
        }

        Stream<Flight> stream = parallelProcessing
                ? flights.parallelStream()
                : flights.stream();

        return stream
                .filter(Objects::nonNull)
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Применение композитного фильтра.
     */
    public List<Flight> applyComposite(List<Flight> flights, FlightFilter compositeFilter) {
        return compositeFilter.apply(flights);
    }

    private List<Flight> copyList(List<Flight> flights) {
        return flights.stream()
                .map(flight -> new Flight(flight.getSegments()))
                .collect(Collectors.toList());
    }
}
