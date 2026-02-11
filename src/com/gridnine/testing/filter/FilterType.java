package com.gridnine.testing.filter;

/**
 * Перечисление всех доступных типов фильтров.
 * Позволяет динамически выбирать фильтры в рантайме.
 */
public enum FilterType {
    DEPARTURE_BEFORE_NOW("Исключает перелёты с вылетом до текущего момента"),
    ARRIVAL_BEFORE_DEPARTURE("Исключает перелёты с прилётом раньше вылета"),
    GROUND_TIME_EXCEEDS("Исключает перелёты с временем на земле более N минут"),
    CUSTOM("Пользовательский фильтр");

    private final String description;

    FilterType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
