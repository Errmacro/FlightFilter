package com.gridnine.testing.printer;

import com.gridnine.testing.Flight;

import java.util.List;
import java.util.Map;

/**
 * Утилитный класс для вывода статистики фильтрации.
 *
 * <p>Рассчитывает количество и процент исключённых перелётов
 * для каждого применённого фильтра, а также итоговые показатели.
 *
 * <p>Использует {@link FlightPrinter} для форматирования заголовков.
 */
public class StatisticPrinter {
    private final FlightPrinter flightPrinter;

    public StatisticPrinter() {
        this.flightPrinter = new FlightPrinter(false, false);
    }

    public void printFilterStatistics(String filterName, List<Flight> original, List<Flight> filtered) {
        int excluded = original.size() - filtered.size();
        double percent = original.isEmpty() ? 0 : (double) excluded / original.size() * 100;

        System.out.printf("  %s:%n", filterName);
        System.out.printf("    Исключено: %d (%.1f%%)%n", excluded, percent);
        System.out.printf("    Оставлено: %d%n", filtered.size());
    }

    public void printDetailedStatistics(List<Flight> original, Map<String, List<Flight>> filterResults) {
        flightPrinter.printHeader("ДЕТАЛЬНАЯ СТАТИСТИКА ФИЛЬТРАЦИИ");
        System.out.printf("Исходное количество перелётов: %d%n%n", original.size());

        filterResults.forEach((name, result) ->
                printFilterStatistics(name, original, result));

        if (!filterResults.isEmpty()) {
            List<Flight> lastResult = filterResults.values().stream()
                    .reduce((first, second) -> second)
                    .orElse(List.of());

            double totalExcludedPercent = original.isEmpty() ? 0 :
                    (double) (original.size() - lastResult.size()) / original.size() * 100;

            System.out.println("\n  ИТОГО:");
            System.out.printf("    Всего исключено: %d (%.1f%%)%n",
                    original.size() - lastResult.size(), totalExcludedPercent);
            System.out.printf("    Прошло все фильтры: %d%n", lastResult.size());
        }
    }
}
