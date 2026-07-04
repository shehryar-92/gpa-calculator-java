package com.gpacalculator;

/**
 * A single course entry: its name, credit hours, and the letter grade earned.
 * Grade points are resolved later via GradeScale, since the same letter
 * can mean different points depending on which scale is active.
 */
public class Course {

    private String name;
    private double creditHours;
    private String letterGrade; // e.g. "A", "B+", or "" if not yet graded

    public Course(String name, double creditHours, String letterGrade) {
        this.name = name;
        this.creditHours = creditHours;
        this.letterGrade = letterGrade == null ? "" : letterGrade.trim();
    }

    public String getName() {
        return name;
    }

    public double getCreditHours() {
        return creditHours;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public boolean hasGrade() {
        return !letterGrade.isEmpty();
    }

    public boolean isValid() {
        return hasGrade() && creditHours > 0;
    }
}
