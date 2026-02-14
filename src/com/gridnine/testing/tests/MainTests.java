package com.gridnine.testing.tests;

import com.gridnine.testing.Flight;
import com.gridnine.testing.FlightBuilder;
import com.gridnine.testing.Segment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainTests {

    @Nested
    @DisplayName("1. Ground Time Calculation Edge Cases")
    class GroundTimeCalculationTests {

        private final LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        private Flight createFlightWithGroundMinutes(long minutes) {
            return new Flight(List.of(
                    new Segment(base, base.plusHours(2)),                    // 10:00-12:00
                    new Segment(base.plusHours(2).plusMinutes(minutes),      // стыковка X мин
                            base.plusHours(2).plusMinutes(minutes).plusHours(3))
            ));
        }

        @Test
        @DisplayName("119 минут - должно проходить фильтр (меньше лимита)")
        void groundTime119_shouldPassFilter() {
            Flight flight = createFlightWithGroundMinutes(119);
            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(120);

            assertTrue(filter.test(flight));
        }

        @Test
        @DisplayName("120 минут - должно проходить фильтр (ровно лимит)")
        void groundTime120_shouldPassFilter() {
            Flight flight = createFlightWithGroundMinutes(120);
            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(120);

            assertTrue(filter.test(flight));
        }

        @Test
        @DisplayName("121 минута - НЕ должно проходить фильтр (больше лимита)")
        void groundTime121_shouldNotPassFilter() {
            Flight flight = createFlightWithGroundMinutes(121);
            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(120);

            assertFalse(filter.test(flight));
        }

        @Test
        @DisplayName("0 минут - должно проходить фильтр")
        void groundTime0_shouldPassFilter() {
            Flight flight = createFlightWithGroundMinutes(0);
            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(120);

            assertTrue(filter.test(flight));
        }

        @Test
        @DisplayName("Один сегмент - всегда проходит (нет времени на земле)")
        void singleSegment_alwaysPasses() {
            Flight flight = new Flight(List.of(
                    new Segment(base, base.plusHours(2))
            ));
            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(1); // даже с малым лимитом

            assertTrue(filter.test(flight));
        }

        @Test
        @DisplayName("Три сегмента - суммарное время считается правильно")
        void threeSegments_sumCalculatedCorrectly() {
            Flight flight = new Flight(List.of(
                    new Segment(base, base.plusHours(2)),
                    new Segment(base.plusHours(2).plusMinutes(30),
                            base.plusHours(2).plusMinutes(30).plusHours(2)),
                    new Segment(base.plusHours(2).plusMinutes(30).plusHours(2).plusMinutes(45),
                            base.plusHours(2).plusMinutes(30).plusHours(2).plusMinutes(45).plusHours(2))
            ));

            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(74);
            var filter2 = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(75);

            assertFalse(filter.test(flight));
            assertTrue(filter2.test(flight));
        }

        @Test
        @DisplayName("Отрицательное время на земле (перекрывающиеся сегменты)")
        void negativeGroundTime_overlappingSegments() {
            Flight flight = new Flight(List.of(
                    new Segment(base, base.plusHours(3)),
                    new Segment(base.plusHours(1), base.plusHours(4))
            ));

            var filter = new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(120);

            assertTrue(filter.test(flight));
        }

        @Test
        @DisplayName("Конструктор не принимает отрицательный лимит")
        void constructor_rejectsNegativeLimit() {
            assertThrows(IllegalArgumentException.class,
                    () -> new com.gridnine.testing.filter.impl.GroundTimeExceedsTwoHoursFilter(-1));
        }
    }

    @Nested
    @DisplayName("2. DepartureBeforeNowFilter with Fixed Clock")
    class DepartureBeforeNowFilterTests {

        private final LocalDateTime fixedNow = LocalDateTime.of(2025, 1, 15, 12, 0);
        private final Clock fixedClock = Clock.fixed(
                fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );

        @Test
        @DisplayName("Вылет ровно сейчас - должен проходить (true)")
        void departureEqualToNow_shouldPass() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);
            Flight flight = new Flight(List.of(
                    new Segment(fixedNow, fixedNow.plusHours(2))
            ));

            assertTrue(filter.test(flight), "Вылет ровно в now должен проходить");
        }

        @Test
        @DisplayName("Вылет после now - должен проходить (true)")
        void departureAfterNow_shouldPass() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);
            Flight flight = new Flight(List.of(
                    new Segment(fixedNow.plusMinutes(1), fixedNow.plusHours(2))
            ));

            assertTrue(filter.test(flight), "Вылет после now должен проходить");
        }

        @Test
        @DisplayName("apply должен отфильтровывать null перелёты")
        void apply_shouldFilterOutNullFlights() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);
            Flight validFlight = new Flight(List.of(
                    new Segment(fixedNow.plusHours(1), fixedNow.plusHours(2))
            ));

            List<Flight> flights = Arrays.asList(null, validFlight, null);
            List<Flight> result = filter.apply(flights);

            assertEquals(1, result.size());
            assertEquals(validFlight, result.get(0));
        }

        @Test
        @DisplayName("Вылет на 1 мс раньше - НЕ должен проходить")
        void departureBeforeNow_shouldNotPass() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);
            Flight flight = new Flight(List.of(
                    new Segment(fixedNow.minusNanos(1_000_000), fixedNow.plusHours(2))
            ));

            assertFalse(filter.test(flight));
        }

        @Test
        @DisplayName("Несколько сегментов: если любой вылет раньше now - фильтр не проходит")
        void multipleSegments_withOneEarlyDeparture_shouldFail() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);
            Flight flight = new Flight(List.of(
                    new Segment(fixedNow.plusHours(1), fixedNow.plusHours(3)),  // будущее
                    new Segment(fixedNow.minusHours(1), fixedNow.plusHours(2)), // прошлое
                    new Segment(fixedNow.plusHours(4), fixedNow.plusHours(6))   // будущее
            ));

            assertFalse(filter.test(flight));
        }

        @Test
        @DisplayName("Null flight - должен возвращать false")
        void nullFlight_shouldReturnFalse() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);

            assertFalse(filter.test(null));
        }

        @Test
        @DisplayName("apply с null списком - возвращает пустой список")
        void apply_withNullList_returnsEmptyList() {
            var filter = new com.gridnine.testing.filter.impl.DepartureBeforeNowFilter(fixedClock);

            List<Flight> result = filter.apply(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("3. CompositeFilter Combinatorics")
    class CompositeFilterTests {

        private final Flight dummyFlight = new Flight(List.of(
                new Segment(LocalDateTime.now(), LocalDateTime.now().plusHours(1))
        ));

        private com.gridnine.testing.filter.FlightFilter createFilter(boolean result) {
            return new com.gridnine.testing.filter.FlightFilter() {
                @Override
                public boolean test(Flight f) { return result; }
                @Override
                public List<Flight> apply(List<Flight> flights) { return flights; }
            };
        }

        @Nested
        @DisplayName("AND Operator")
        class AndOperatorTests {

            @Test
            @DisplayName("Пустой список фильтров - должен возвращать false")
            void emptyFilters_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Все null фильтры - должен возвращать false")
            void allNullFilters_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                        null, null, null
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Смесь null и true фильтров - должно работать как AND с true")
            void nullAndTrueFilters_shouldWorkAsAndWithTrue() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                        null, createFilter(true), null, createFilter(true)
                );

                assertTrue(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Смесь null, true и false - должно вернуть false")
            void nullTrueAndFalse_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                        null, createFilter(true), createFilter(false), createFilter(true)
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Один false среди многих true - должно вернуть false")
            void oneFalseAmongManyTrue_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                        createFilter(true), createFilter(true), createFilter(false), createFilter(true)
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Все true - должно вернуть true")
            void allTrue_returnsTrue() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                        createFilter(true), createFilter(true), createFilter(true)
                );

                assertTrue(composite.test(dummyFlight));
            }
        }

        @Nested
        @DisplayName("OR Operator")
        class OrOperatorTests {

            @Test
            @DisplayName("Пустой список фильтров - должен возвращать false")
            void emptyFilters_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Все null фильтры - должен возвращать false")
            void allNullFilters_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR,
                        null, null, null
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Смесь null и false - должно вернуть false")
            void nullAndFalse_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR,
                        null, createFilter(false), null, createFilter(false)
                );

                assertFalse(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Смесь null, false и true - должно вернуть true")
            void nullFalseAndTrue_returnsTrue() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR,
                        null, createFilter(false), createFilter(true), createFilter(false)
                );

                assertTrue(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Один true среди многих false - должно вернуть true")
            void oneTrueAmongManyFalse_returnsTrue() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR,
                        createFilter(false), createFilter(false), createFilter(true), createFilter(false)
                );

                assertTrue(composite.test(dummyFlight));
            }

            @Test
            @DisplayName("Все false - должно вернуть false")
            void allFalse_returnsFalse() {
                var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                        com.gridnine.testing.filter.CompositeFlightFilter.Operator.OR,
                        createFilter(false), createFilter(false), createFilter(false)
                );

                assertFalse(composite.test(dummyFlight));
            }
        }

        @Test
        @DisplayName("Null flight в test - должен возвращать false")
        void nullFlight_returnsFalse() {
            var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                    com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                    createFilter(true)
            );

            assertFalse(composite.test(null));
        }

        @Test
        @DisplayName("apply с null списком - возвращает пустой список")
        void apply_withNullList_returnsEmptyList() {
            var composite = new com.gridnine.testing.filter.CompositeFlightFilter(
                    com.gridnine.testing.filter.CompositeFlightFilter.Operator.AND,
                    createFilter(true)
            );

            List<Flight> result = composite.apply(null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("4. FlightBuilder Edge Cases")
    class FlightBuilderTests {

        private final LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        @Test
        @DisplayName("createFlight с null массивом - должен вернуть пустой перелёт")
        void createFlight_withNullArray_returnsEmptyFlight() {
            Flight flight = FlightBuilder.createFlight((LocalDateTime[]) null);

            assertNotNull(flight);
            assertTrue(flight.getSegments().isEmpty());
        }

        @Test
        @DisplayName("createFlight с пустым массивом - должен вернуть пустой перелёт")
        void createFlight_withEmptyArray_returnsEmptyFlight() {
            Flight flight = FlightBuilder.createFlight(new LocalDateTime[0]);

            assertNotNull(flight);
            assertTrue(flight.getSegments().isEmpty());
        }

        @Test
        @DisplayName("createFlight с 1 датой (нечётное число) - должен выбросить исключение")
        void createFlight_withOddNumberOfDates_throwsException() {
            LocalDateTime[] dates = { base };

            assertThrows(IllegalArgumentException.class,
                    () -> FlightBuilder.createFlight(dates),
                    "you must pass an even number of dates");
        }

        @Test
        @DisplayName("createFlight с 3 датами (нечётное число) - должен выбросить исключение")
        void createFlight_withThreeDates_throwsException() {
            LocalDateTime[] dates = { base, base.plusHours(1), base.plusHours(2) };

            assertThrows(IllegalArgumentException.class,
                    () -> FlightBuilder.createFlight(dates));
        }

        @Test
        @DisplayName("createFlight с 2 датами - создаёт один сегмент")
        void createFlight_withTwoDates_createsOneSegment() {
            LocalDateTime dep = base;
            LocalDateTime arr = base.plusHours(2);

            Flight flight = FlightBuilder.createFlight(dep, arr);

            assertEquals(1, flight.getSegments().size());
            assertEquals(dep, flight.getSegments().get(0).getDepartureDate());
            assertEquals(arr, flight.getSegments().get(0).getArrivalDate());
        }

        @Test
        @DisplayName("createFlight с 4 датами - создаёт два сегмента")
        void createFlight_withFourDates_createsTwoSegments() {
            LocalDateTime d1 = base;
            LocalDateTime a1 = base.plusHours(2);
            LocalDateTime d2 = base.plusHours(3);
            LocalDateTime a2 = base.plusHours(5);

            Flight flight = FlightBuilder.createFlight(d1, a1, d2, a2);

            assertEquals(2, flight.getSegments().size());
            assertAll(
                    () -> assertEquals(d1, flight.getSegments().get(0).getDepartureDate()),
                    () -> assertEquals(a1, flight.getSegments().get(0).getArrivalDate()),
                    () -> assertEquals(d2, flight.getSegments().get(1).getDepartureDate()),
                    () -> assertEquals(a2, flight.getSegments().get(1).getArrivalDate())
            );
        }

        @Test
        @DisplayName("createSafeFlight с null - возвращает пустой перелёт")
        void createSafeFlight_withNull_returnsEmptyFlight() {
            Flight flight = FlightBuilder.createSafeFlight(null);

            assertNotNull(flight);
            assertTrue(flight.getSegments().isEmpty());
        }

        @Test
        @DisplayName("createSafeFlight с пустым списком - возвращает пустой перелёт")
        void createSafeFlight_withEmptyList_returnsEmptyFlight() {
            Flight flight = FlightBuilder.createSafeFlight(List.of());

            assertNotNull(flight);
            assertTrue(flight.getSegments().isEmpty());
        }

        @Test
        @DisplayName("createSafeFlight с валидными сегментами - создаёт перелёт")
        void createSafeFlight_withValidSegments_createsFlight() {
            List<Segment> segments = List.of(
                    new Segment(base, base.plusHours(2))
            );

            Flight flight = FlightBuilder.createSafeFlight(segments);

            assertEquals(1, flight.getSegments().size());
            assertEquals(segments.get(0), flight.getSegments().get(0));
        }

        @Test
        @DisplayName("createFlights - возвращает не-null список с 6 элементами")
        void createFlights_returnsNonNullListWith6Elements() {
            List<Flight> flights = FlightBuilder.createFlights();

            assertNotNull(flights);
            assertEquals(6, flights.size());
        }
    }

    @Nested
    @DisplayName("5. Segment Constructor Null Checks")
    class SegmentConstructorTests {

        @Test
        @DisplayName("Должен выбросить NPE при null departure")
        void shouldThrowNPE_whenDepartureIsNull() {
            LocalDateTime validTime = LocalDateTime.now();
            assertThrows(NullPointerException.class,
                    () -> new Segment(null, validTime),
                    "Departure date cannot be null");
        }

        @Test
        @DisplayName("Должен выбросить NPE при null arrival")
        void shouldThrowNPE_whenArrivalIsNull() {
            LocalDateTime validTime = LocalDateTime.now();
            assertThrows(NullPointerException.class,
                    () -> new Segment(validTime, null),
                    "Arrival date cannot be null");
        }

        @Test
        @DisplayName("Должен выбросить NPE при обоих null")
        void shouldThrowNPE_whenBothAreNull() {
            assertThrows(NullPointerException.class,
                    () -> new Segment(null, null),
                    "Dates cannot be null");
        }

        @Test
        @DisplayName("Должен успешно создать сегмент с валидными датами")
        void shouldCreateSegment_withValidDates() {
            LocalDateTime dep = LocalDateTime.of(2025, 1, 1, 10, 0);
            LocalDateTime arr = LocalDateTime.of(2025, 1, 1, 12, 0);

            Segment segment = new Segment(dep, arr);

            assertAll(
                    () -> assertEquals(dep, segment.getDepartureDate()),
                    () -> assertEquals(arr, segment.getArrivalDate())
            );
        }
    }
}
