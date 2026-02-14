## 📖 Описание проекта

FlightFilter - система-модуль для фильтрации набора перелётов

## 🧩 Функциональность
 Модуль выдает в консоль результаты обработки тестового набора перелётов.
 Действует со следующими ограничениями:
- Исключен вылет до текущего момента времени.
- Исключены сегменты с датой прилёта раньше даты вылета.
- Исключены перелеты, где общее время, проведённое на земле, превышает два часа

## 🛠 Стек технологий
- Java 17
- JUnit5.8.1
 

## 🗂 Структура проекта
com.gridnine.testing

                    ├── Flight.java
                    
                    ├── Segment.java
                    
                    ├── FlightBuilder.java
                    
                    ├── Main.java
                    
                    │
                    
                    ├── config/
                    
                    │   ├── FilterConfiguration.java
                    
                    │   └── FilterConfiguration.FilterRule.java
                    
                    │
                    
                    ├── filter/
                    
                    │   ├── FlightFilter.java
                    
                    │   ├── CompositeFlightFilter.java
                    
                    │   ├── NotFlightFilter.java
                    
                    │   ├── FilterFactory.java
                    
                    │   ├── FilterType.java
                    
                    │   ├── FlightFilterService.java
                    
                    │   │
                    
                    │   └── impl/
                    
                    │       ├── ArrivalBeforeDepartureFilter.java
                    
                    │       ├── DepartureBeforeNowFilter.java
                    
                    │       └── GroundTimeExceedsTwoHoursFilter.java
                    
                    │
                    
                    ├── printer/
                    
                        ├── FlightPrinter.java
                        
                        └── StatisticPrinter.java
                        
                    └── tests/
                    
                        └── MainTests.java
                        
                        
    

