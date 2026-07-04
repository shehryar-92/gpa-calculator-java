package com.gpacalculator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Writes the current course list + computed GPA out to a PDF file.
 * Kept as one focused class: build a document, draw a simple table, save it.
 */
public class PdfExporter {

    private static final float MARGIN = 50f;
    private static final float ROW_HEIGHT = 22f;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();

    // Column x-positions
    private static final float COL_NAME_X = MARGIN;
    private static final float COL_CREDITS_X = MARGIN + 260;
    private static final float COL_GRADE_X = MARGIN + 350;
    private static final float COL_POINTS_X = MARGIN + 430;

    public static void export(File file,
                               List<Course> courses,
                               GradeScale scale,
                               double gpa) throws IOException {

        Map<String, Double> scalePoints = scale.points();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            PDFont bold = PDType1Font.HELVETICA_BOLD;
            PDFont regular = PDType1Font.HELVETICA;

            float y = PAGE_HEIGHT - MARGIN;

            // Title
            cs.beginText();
            cs.setFont(bold, 18);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("GPA Report");
            cs.endText();
            y -= 22;

            // Date + scale used
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            cs.beginText();
            cs.setFont(regular, 10);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Generated " + date + "  |  Scale: " + scale.toString());
            cs.endText();
            y -= 28;

            // Table header
            cs.setFont(bold, 11);
            cs = drawRow(cs, y, "Course", "Credits", "Grade", "Points");
            y -= 6;
            cs.moveTo(MARGIN, y);
            cs.lineTo(PAGE_WIDTH - MARGIN, y);
            cs.stroke();
            y -= ROW_HEIGHT - 6;

            cs.setFont(regular, 11);

            for (Course c : courses) {
                if (y < MARGIN + 60) {
                    // start a new page if we run out of room
                    cs.close();
                    page = new PDPage(PDRectangle.LETTER);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.setFont(regular, 11);
                    y = PAGE_HEIGHT - MARGIN;
                }

                String creditsStr = trimNumber(c.getCreditHours());
                String gradeStr = c.hasGrade() ? c.getLetterGrade() : "-";
                Double points = scalePoints.get(c.getLetterGrade());
                String pointsStr = (c.isValid() && points != null)
                        ? String.format("%.2f", points * c.getCreditHours())
                        : "-";

                cs = drawRow(cs, y, c.getName().isEmpty() ? "(untitled)" : c.getName(),
                        creditsStr, gradeStr, pointsStr);
                y -= ROW_HEIGHT;
            }

            // Summary
            y -= 10;
            cs.moveTo(MARGIN, y);
            cs.lineTo(PAGE_WIDTH - MARGIN, y);
            cs.stroke();
            y -= 24;

            double totalCredits = GpaCalculator.totalCreditHours(courses);

            cs.beginText();
            cs.setFont(bold, 12);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Total Credit Hours: " + trimNumber(totalCredits));
            cs.endText();
            y -= 20;

            cs.beginText();
            cs.setFont(bold, 14);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(String.format("GPA: %.2f", gpa));
            cs.endText();

            cs.close();
            doc.save(file);
        }
    }

    private static PDPageContentStream drawRow(PDPageContentStream cs, float y,
                                                String name, String credits,
                                                String grade, String points) throws IOException {
        cs.beginText();
        cs.newLineAtOffset(COL_NAME_X, y);
        cs.showText(truncate(name, 32));
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(COL_CREDITS_X, y);
        cs.showText(credits);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(COL_GRADE_X, y);
        cs.showText(grade);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(COL_POINTS_X, y);
        cs.showText(points);
        cs.endText();

        return cs;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String trimNumber(double d) {
        // Show "3" instead of "3.0", but "3.5" stays as-is
        if (d == Math.floor(d)) return String.valueOf((int) d);
        return String.valueOf(d);
    }
}
