package com.gpacalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The GUI. Table-based course entry (name, credit hours, grade), a scale
 * selector that swaps which letter grades are offered, a live GPA readout,
 * and a button to export the current table to a PDF.
 */
public class GpaApp extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<GradeScale> scaleSelector;
    private final JLabel gpaLabel;

    private GradeScale currentScale = GradeScale.ORIGINAL;

    public GpaApp() {
        super("GPA Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        // --- Top bar: scale selector ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(new JLabel("Grading Scale:"));
        scaleSelector = new JComboBox<>(GradeScale.values());
        scaleSelector.setSelectedItem(GradeScale.ORIGINAL);
        scaleSelector.addActionListener(e -> onScaleChanged());
        topBar.add(scaleSelector);
        root.add(topBar, BorderLayout.NORTH);

        // --- Table ---
        tableModel = new DefaultTableModel(new Object[]{"Course Name", "Credit Hours", "Grade"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(26);
        refreshGradeEditor(); // sets up the Grade column's dropdown editor
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Bottom: buttons + GPA readout ---
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowBtn = new JButton("+ Add Course");
        JButton removeRowBtn = new JButton("Remove Selected");
        JButton calculateBtn = new JButton("Calculate GPA");
        JButton exportBtn = new JButton("Export to PDF");

        addRowBtn.addActionListener(e -> tableModel.addRow(new Object[]{"", "", ""}));
        removeRowBtn.addActionListener(e -> removeSelectedRow());
        calculateBtn.addActionListener(e -> updateGpaLabel());
        exportBtn.addActionListener(e -> exportToPdf());

        buttonRow.add(addRowBtn);
        buttonRow.add(removeRowBtn);
        buttonRow.add(calculateBtn);
        buttonRow.add(exportBtn);

        gpaLabel = new JLabel("GPA: --");
        gpaLabel.setFont(gpaLabel.getFont().deriveFont(Font.BOLD, 16f));
        gpaLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        bottom.add(buttonRow, BorderLayout.NORTH);
        bottom.add(gpaLabel, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        // Start with a couple of blank rows so the UI isn't empty
        tableModel.addRow(new Object[]{"", "", ""});
        tableModel.addRow(new Object[]{"", "", ""});
    }

    /** Rebuilds the Grade column's combo-box editor to match the active scale. */
    private void refreshGradeEditor() {
        Set<String> keys = currentScale.points().keySet();

        JComboBox<String> gradeBox = new JComboBox<>();
        gradeBox.addItem("");
        for (String letter : keys) gradeBox.addItem(letter);

        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(gradeBox));
    }

    /** When the scale changes: refresh the dropdown, and clear any grade values that no longer exist. */
    private void onScaleChanged() {
        GradeScale selected = (GradeScale) scaleSelector.getSelectedItem();
        if (selected == null || selected == currentScale) return;
        currentScale = selected;

        Set<String> validKeys = currentScale.points().keySet();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Object grade = tableModel.getValueAt(row, 2);
            if (grade != null && !grade.toString().isEmpty() && !validKeys.contains(grade.toString())) {
                tableModel.setValueAt("", row, 2);
            }
        }

        refreshGradeEditor();
        updateGpaLabel();
    }

    private void removeSelectedRow() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            tableModel.removeRow(row);
        }
    }

    /** Reads the table into a List<Course>, skipping rows that can't be parsed. */
    private List<Course> collectCourses() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        List<Course> courses = new ArrayList<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String name = String.valueOf(tableModel.getValueAt(row, 0));
            String creditsStr = String.valueOf(tableModel.getValueAt(row, 1));
            String grade = String.valueOf(tableModel.getValueAt(row, 2));

            double credits;
            try {
                credits = Double.parseDouble(creditsStr.trim());
            } catch (NumberFormatException e) {
                credits = 0; // invalid/blank credit hours -> row is ignored by GpaCalculator
            }

            courses.add(new Course(name == null ? "" : name, credits, grade));
        }
        return courses;
    }

    private void updateGpaLabel() {
        List<Course> courses = collectCourses();
        boolean anyValid = courses.stream().anyMatch(Course::isValid);

        if (!anyValid) {
            gpaLabel.setText("GPA: -- (add at least one course with credit hours and a grade)");
            return;
        }

        double gpa = GpaCalculator.calculate(courses, currentScale.points());
        gpaLabel.setText(String.format("GPA: %.2f", gpa));
    }

    private void exportToPdf() {
        List<Course> courses = collectCourses();
        boolean anyValid = courses.stream().anyMatch(Course::isValid);

        if (!anyValid) {
            JOptionPane.showMessageDialog(this,
                    "Add at least one course with credit hours and a grade before exporting.",
                    "Nothing to export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("gpa-report.pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        double gpa = GpaCalculator.calculate(courses, currentScale.points());

        try {
            PdfExporter.export(file, courses, currentScale, gpa);
            JOptionPane.showMessageDialog(this,
                    "Saved to " + file.getAbsolutePath(),
                    "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Couldn't save PDF: " + e.getMessage(),
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GpaApp().setVisible(true));
    }
}
