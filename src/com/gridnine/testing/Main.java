package com.gridnine.testing;

import com.gridnine.testing.config.FilterConfiguration;
import com.gridnine.testing.filter.FilterFactory;
import com.gridnine.testing.filter.FilterType;
import com.gridnine.testing.filter.FlightFilter;
import com.gridnine.testing.filter.FlightFilterService;
import com.gridnine.testing.printer.FlightPrinter;
import com.gridnine.testing.printer.StatisticPrinter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Главный класс демонстрирующий динамическую фильтрацию перелётов.
 * Полностью соответствует условиям задачи:
 * 1. Динамический выбор правил
 * 2. Возможность добавления новых правил
 * 3. Эффективная обработка больших наборов данных
 * 4. Разделение ответственности
 * 5. Композиция фильтров
 */
public class Main {
    private static final FlightPrinter flightPrinter = new FlightPrinter(true, true);
    private static final StatisticPrinter statisticsPrinter = new StatisticPrinter();
    private static final FlightFilterService filterService = new FlightFilterService(true);

    public static void main(String[] args) {

        List<Flight> originalFlights = FlightBuilder.createFlights();
        demonstrateDynamicFilterCreation(originalFlights);
        demonstrateFilterComposition(originalFlights);
        demonstrateConfigurableRules(originalFlights);

    }

    /**
     * Демонстрация динамического создания фильтров через фабрику.
     * Правила выбираются динамически из перечисления FilterType.
     */
    private static void demonstrateDynamicFilterCreation(List<Flight> originalFlights) {
        flightPrinter.printHeader("ДИНАМИЧЕСКОЕ СОЗДАНИЕ ФИЛЬТРОВ");


        System.out.println("Доступные типы фильтров:");
        FilterFactory.getAvailableFilters().forEach((type, desc) ->
                System.out.printf("  - %s: %s%n", type, desc));
        System.out.println();


        FlightFilter departureFilter = FilterFactory.createFilter(FilterType.DEPARTURE_BEFORE_NOW);
        FlightFilter arrivalFilter = FilterFactory.createFilter(FilterType.ARRIVAL_BEFORE_DEPARTURE);
        FlightFilter groundFilter = FilterFactory.createFilter(FilterType.GROUND_TIME_EXCEEDS);


        List<Flight> validDeparture = filterService.applyFilter(originalFlights, departureFilter);
        List<Flight> validArrival = filterService.applyFilter(originalFlights, arrivalFilter);
        List<Flight> validGround = filterService.applyFilter(originalFlights, groundFilter);


        flightPrinter.printFlights(validDeparture, "ПЕРЕЛЁТЫ БЕЗ ВЫЛЕТОВ В ПРОШЛОМ");
        flightPrinter.printFlights(validArrival, "ПЕРЕЛЁТЫ С КОРРЕКТНЫМИ СЕГМЕНТАМИ");
        flightPrinter.printFlights(validGround, "ПЕРЕЛЁТЫ С ВРЕМЕНЕМ НА ЗЕМЛЕ ≤ 2 ЧАСОВ");


        Map<String, List<Flight>> stats = new LinkedHashMap<>();
        stats.put("Вылет в прошлом", validDeparture);
        stats.put("Прилёт раньше вылета", validArrival);
        stats.put("Время на земле < 2ч", validGround);
        statisticsPrinter.printDetailedStatistics(originalFlights, stats);
    }

    /**
     * Демонстрация композиции фильтров (AND, OR, NOT).
     * Показывает как динамически комбинировать правила.
     */
    private static void demonstrateFilterComposition(List<Flight> originalFlights) {
        flightPrinter.printHeader("КОМПОЗИЦИЯ ФИЛЬТРОВ");


        FlightFilter departureFilter = FilterFactory.createFilter(FilterType.DEPARTURE_BEFORE_NOW);
        FlightFilter arrivalFilter = FilterFactory.createFilter(FilterType.ARRIVAL_BEFORE_DEPARTURE);
        FlightFilter groundFilter = FilterFactory.createFilter(FilterType.GROUND_TIME_EXCEEDS, 120);


        FlightFilter andFilter = FlightFilter.allOf(departureFilter, arrivalFilter, groundFilter);
        List<Flight> andResult = filterService.applyComposite(originalFlights, andFilter);
        flightPrinter.printFlights(andResult, "AND: ВСЕ ПРАВИЛА (ВЫЛЕТ+ПРИЛЁТ+ЗЕМЛЯ)");


        FlightFilter orFilter = FlightFilter.anyOf(departureFilter, arrivalFilter);
        List<Flight> orResult = filterService.applyComposite(originalFlights, orFilter);
        flightPrinter.printFlights(orResult, "OR: ВЫЛЕТ ИЛИ ПРИЛЁТ");


        FlightFilter notFilter = FlightFilter.not(groundFilter);
        List<Flight> notResult = filterService.applyComposite(originalFlights, notFilter);
        flightPrinter.printFlights(notResult, "NOT: ВРЕМЯ НА ЗЕМЛЕ > 2 ЧАСОВ (ИСКЛЮЧАЕМ)");


        FlightFilter complexFilter = FlightFilter.anyOf(
                FlightFilter.allOf(departureFilter, arrivalFilter),
                groundFilter
        );
        List<Flight> complexResult = filterService.applyComposite(originalFlights, complexFilter);
        flightPrinter.printFlights(complexResult, "СЛОЖНАЯ КОМПОЗИЦИЯ: (ВЫЛЕТ+ПРИЛЁТ) ИЛИ (ЗЕМЛЯ≤2Ч)");
    }

    /**
     * Демонстрация конфигурируемых правил.
     * Показывает как задавать правила через конфигурацию в рантайме.
     */
    private static void demonstrateConfigurableRules(List<Flight> originalFlights) {
        flightPrinter.printHeader("КОНФИГУРИРУЕМЫЕ ПРАВИЛА");


        FilterConfiguration config = new FilterConfiguration()
                .addRule(FilterType.DEPARTURE_BEFORE_NOW)
                .addRule(FilterType.ARRIVAL_BEFORE_DEPARTURE)
                .addRule(FilterType.GROUND_TIME_EXCEEDS, 90); // 90 минут вместо 120

        System.out.println("Применяется конфигурация с правилами:");
        config.getRules().forEach(rule ->
                System.out.printf("  - %s (параметры: %s, отрицание: %s)%n",
                        rule.getType(),
                        rule.getParams().length > 0 ? rule.getParams()[0] : "нет",
                        rule.isNegated()));
        System.out.println();


        List<FlightFilter> filters = config.getRules().stream()
                .map(rule -> {
                    FlightFilter filter = FilterFactory.createFilter(rule.getType(), rule.getParams());
                    return rule.isNegated() ? FlightFilter.not(filter) : filter;
                })
                .toList();


        List<Flight> configResult = filterService.applyFilters(originalFlights, filters);
        flightPrinter.printFlights(configResult, "РЕЗУЛЬТАТ ПРИМЕНЕНИЯ КОНФИГУРАЦИИ");
    }

}
