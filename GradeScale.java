package com.gpacalculator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines the available grading scales. Two options are supported:
 *
 *  ORIGINAL - matches the scale used in the web version of this project.
 *  STANDARD - the common US +/- 4.0 scale, offered as an alternative.
 *
 * Letter sets differ between scales (e.g. ORIGINAL has no A-/B-/C-),
 * so the UI must refresh its grade dropdown whenever the scale changes.
 */
public enum GradeScale {

    ORIGINAL("No Minus Grades (A, B+, B, C+...)") {
        @Override
        public Map<String, Double> points() {
            Map<String, Double> m = new LinkedHashMap<>();
            m.put("A+", 4.0);
            m.put("A", 4.0);
            m.put("B+", 3.5);
            m.put("B", 3.0);
            m.put("C+", 2.5);
            m.put("C", 2.0);
            m.put("D+", 1.5);
            m.put("D", 1.0);
            m.put("F", 0.0);
            return m;
        }
    },

    STANDARD("Plus/Minus Grades (A, A-, B+, B-...)") {
        @Override
        public Map<String, Double> points() {
            Map<String, Double> m = new LinkedHashMap<>();
            m.put("A", 4.0);
            m.put("A-", 3.7);
            m.put("B+", 3.3);
            m.put("B", 3.0);
            m.put("B-", 2.7);
            m.put("C+", 2.3);
            m.put("C", 2.0);
            m.put("C-", 1.7);
            m.put("D+", 1.3);
            m.put("D", 1.0);
            m.put("D-", 0.7);
            m.put("F", 0.0);
            return m;
        }
    };

    private final String displayName;

    GradeScale(String displayName) {
        this.displayName = displayName;
    }

    /** Letter grade -> grade points map for this scale, in display order. */
    public abstract Map<String, Double> points();

    @Override
    public String toString() {
        return displayName;
    }
}
