package com.gridnine.testing.config;

import com.gridnine.testing.filter.FilterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilterConfiguration {
    private final List<FilterRule> rules = new ArrayList<>();

    public static class FilterRule {
        private final FilterType type;
        private final Object[] params;
        private final boolean negated;

        public FilterRule(FilterType type, Object... params) {
            this(type, false, params);
        }

        public FilterRule(FilterType type, boolean negated, Object... params) {
            this.type = Objects.requireNonNull(type, "Filter type cannot be null");
            this.negated = negated;
            this.params = params != null ? params.clone() : new Object[0];
        }

        public FilterType getType() {
            return type;
        }

        public Object[] getParams() {
            return params.clone();
        }

        public boolean isNegated() {
            return negated;
        }
    }

    public FilterConfiguration addRule(FilterType type, Object... params) {
        rules.add(new FilterRule(type, params));
        return this;
    }

    public FilterConfiguration addNegatedRule(FilterType type, Object... params) {
        rules.add(new FilterRule(type, true, params));
        return this;
    }

    public List<FilterRule> getRules() {
        return new ArrayList<>(rules);
    }

    public void clear() {
        rules.clear();
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }
}
