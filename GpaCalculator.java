package com.gpacalculator;

import java.util.List;
import java.util.Map;

/**
 * Pure GPA math. Takes a list of courses and a scale's point map, returns the GPA.
 * Kept separate from the UI so the logic stays simple and independently checkable.
 */
public class GpaCalculator {

    /**
     * Computes weighted GPA = sum(creditHours * gradePoints) / sum(creditHours).
     * Courses with no grade selected, or non-positive credit hours, are skipped
     * (treated as "in progress" / not yet counted).
     *
     * @return the GPA, or 0.0 if there are no valid graded courses.
     */
    public static double calculate(List<Course> courses, Map<String, Double> scalePoints) {
        double totalPoints = 0.0;
        double totalCredits = 0.0;

        for (Course c : courses) {
            if (!c.isValid()) continue;
            Double points = scalePoints.get(c.getLetterGrade());
            if (points == null) continue; // grade not part of the active scale
            totalPoints += points * c.getCreditHours();
            totalCredits += c.getCreditHours();
        }

        if (totalCredits == 0.0) return 0.0;
        return totalPoints / totalCredits;
    }

    public static double totalCreditHours(List<Course> courses) {
        double total = 0.0;
        for (Course c : courses) {
            if (c.isValid()) total += c.getCreditHours();
        }
        return total;
    }
}
