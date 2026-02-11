package com.gridnine.testing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Утилитный класс для создания тестовых наборов перелётов.
 * Содержит фабричные методы для генерации предопределённых сценариев,
 * используемых в демонстрационных целях и модульном тестировании.
 */
public class FlightBuilder {
    public static List<Flight> createFlights() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);

        return Arrays.asList(
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
                createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
                createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
                createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                        threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                        threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7))
        );
    }

    private static Flight createFlight(final LocalDateTime... dates) {
        if (dates == null) {
            return new Flight(List.of()); // Пустой перелёт
        }

        if ((dates.length % 2) != 0) {
            throw new IllegalArgumentException(
                    "you must pass an even number of dates");
        }

        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
            segments.add(new Segment(dates[i], dates[i + 1]));
        }

        return new Flight(segments);
    }

    public static Flight createSafeFlight(List<Segment> segments) {
        return new Flight(segments != null ? segments : List.of());
    }
}
