# GPA Calculator (Java Swing)

A desktop GPA calculator with a switchable grading scale and one-click PDF export.

## Features
- Table-based course entry: name, credit hours, letter grade
- Two grading scales, switchable at any time:
       a standard US scale +/- (e.g A+, A ,A-, B+ , B .....)
       a non-standard scale (e.g A+, A , B+ , B .....)
- Live GPA calculation
- Export the current course list and GPA to a formatted PDF report

## Tech Stack
- Java 25
- Swing (GUI)
- Apache PDFBox (PDF export)
- Maven (build)

## Run locally

Requires Java 25+ and Maven.

```bash
mvn clean package
java -jar target/gpa-calculator-1.0.0.jar
```

Or run `com.gpacalculator.GpaApp` directly from your IDE.

## How it works
- Add courses with the **+ Add Course** button, fill in name / credit hours / grade.
- Switch the grading scale from the dropdown at the top — grade options update automatically.
- Click **Calculate GPA** for a live readout, or **Export to PDF** to save a report.
