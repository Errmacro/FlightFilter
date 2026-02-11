package com.gridnine.testing;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Представляет перелёт, состоящий из одного или нескольких сегментов.
 * Перелёт может быть как прямым (один сегмент), так и с пересадками (несколько сегментов).
 * Объект Flight является неизменяемым (immutable).
 */
public class Flight {
    private final List<Segment> segments;

    public Flight(final List<Segment> segs) {
        this.segments = segs != null ? List.copyOf(segs) : List.of();
    }

    public List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Object::toString)
                .collect(Collectors.joining(" "));
    }
}
