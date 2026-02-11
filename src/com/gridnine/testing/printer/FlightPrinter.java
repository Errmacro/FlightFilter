package com.gridnine.testing.printer;

import com.gridnine.testing.Flight;
import com.gridnine.testing.Segment;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Утилитный класс для форматированного вывода информации о перелётах.
 *
 * <p>Поддерживает два режима:
 * <ul>
 *   <li>Детальный - с отображением каждого сегмента и времени стыковок</li>
 *   <li>Краткий - только сводная информация</li>
 * </ul>
 *
 * <p>Все методы выводят данные в {@link System#out}.
 */
public class FlightPrinter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final boolean detailed;
    private final boolean showGroundTime;

    public FlightPrinter() {
        this(true, true);
    }

    public FlightPrinter(boolean detailed, boolean showGroundTime) {
        this.detailed = detailed;
        this.showGroundTime = showGroundTime;
    }

    public void printHeader(String title) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(title);
        System.out.println("=".repeat(50));
    }

    public void printFlights(List<Flight> flights, String title) {
        printHeader(title);
        System.out.println("Количество: " + flights.size());
        System.out.println();

        if (flights.isEmpty()) {
            System.out.println("  (нет перелётов)");
            return;
        }

        for (int i = 0; i < flights.size(); i++) {
            printFlight(flights.get(i), i + 1);
        }
    }

    public void printFlight(Flight flight, int index) {
        List<Segment> segments = flight.getSegments();

        System.out.printf("  %d. %s%n", index, formatFlightSummary(flight));

        if (detailed) {
            for (int j = 0; j < segments.size(); j++) {
                Segment seg = segments.get(j);
                System.out.printf("      Сегмент %d: %s → %s%n",
                        j + 1,
                        seg.getDepartureDate().format(DATE_FORMATTER),
                        seg.getArrivalDate().format(DATE_FORMATTER));

                if (showGroundTime && j < segments.size() - 1) {
                    long groundMinutes = calculateGroundTimeBetween(seg, segments.get(j + 1));
                    if (groundMinutes > 0) {
                        System.out.printf("        ↓ стыковка: %d мин (%d ч %d мин)%n",
                                groundMinutes, groundMinutes / 60, groundMinutes % 60);
                    }
                }
            }

            if (showGroundTime && segments.size() > 1) {
                long totalGroundTime = calculateTotalGroundTime(flight);
                System.out.printf("      Общее время на земле: %d мин (%d ч %d мин)%n",
                        totalGroundTime, totalGroundTime / 60, totalGroundTime % 60);
            }
            System.out.println();
        }
    }

    public String formatFlightSummary(Flight flight) {
        List<Segment> segments = flight.getSegments();
        if (segments.isEmpty()) {
            return "[пустой перелёт]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(segments.get(0).getDepartureDate().format(DATE_FORMATTER));
        sb.append(" → ");
        sb.append(segments.get(segments.size() - 1).getArrivalDate().format(DATE_FORMATTER));
        sb.append(" [");
        sb.append(segments.size());
        sb.append(" сегм.]");
        return sb.toString();
    }

    public String formatSegment(Segment segment) {
        return '[' +
                segment.getDepartureDate().format(ISO_FORMATTER) +
                '|' +
                segment.getArrivalDate().format(ISO_FORMATTER) +
                ']';
    }

    private long calculateGroundTimeBetween(Segment seg1, Segment seg2) {
        return Duration.between(seg1.getArrivalDate(), seg2.getDepartureDate()).toMinutes();
    }

    private long calculateTotalGroundTime(Flight flight) {
        List<Segment> segments = flight.getSegments();
        if (segments.size() < 2) return 0;

        long total = 0;
        for (int i = 0; i < segments.size() - 1; i++) {
            total += calculateGroundTimeBetween(segments.get(i), segments.get(i + 1));
        }
        return total;
    }
}
