package com.gridnine.testing;

import java.util.List;
import java.util.stream.Collectors;

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
