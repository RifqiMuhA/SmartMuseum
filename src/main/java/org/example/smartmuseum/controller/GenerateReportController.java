package org.example.smartmuseum.controller;

import com.itextpdf.text.Font;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.smartmuseum.model.entity.Employee;
import org.example.smartmuseum.model.entity.Attendance;
import org.example.smartmuseum.model.service.EmployeeService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateReportController implements Initializable {

    // FXML Components
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private ComboBox<String> cmbEmployeeFilter;
    @FXML private RadioButton rbExcel;
    @FXML private RadioButton rbPDF;
    @FXML private TextField txtBonusPerAttendance;
    @FXML private TextField txtWorkingDays;
    @FXML private Button btnGenerateReport;
    @FXML private Button btnPreview;
    @FXML private Label lblGenerationStatus;
    @FXML private Label lblReportStatus;

    // Summary Labels
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblReportPeriod;
    @FXML private Label lblTotalWorkingDays;
    @FXML private Label lblAvgAttendanceRate;

    // Table
    @FXML private TableView<EmployeeReportData> tableEmployeeReport;
    @FXML private TableColumn<EmployeeReportData, String> colEmployeeName;
    @FXML private TableColumn<EmployeeReportData, String> colPosition;
    @FXML private TableColumn<EmployeeReportData, String> colBaseSalary;
    @FXML private TableColumn<EmployeeReportData, Integer> colAttendanceCount;
    @FXML private TableColumn<EmployeeReportData, String> colAttendanceRate;
    @FXML private TableColumn<EmployeeReportData, String> colAttendanceBonus;
    @FXML private TableColumn<EmployeeReportData, String> colTotalSalary;

    // Services
    private EmployeeService employeeService;
    private ObservableList<EmployeeReportData> reportData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        employeeService = new EmployeeService();
        reportData = FXCollections.observableArrayList();

        setupComponents();
        setupTable();
        loadEmployees();
        setDefaultValues();
    }

    private void setupComponents() {
        // Setup date range (default to current month)
        LocalDate now = LocalDate.now();
        dateFrom.setValue(now.withDayOfMonth(1)); // First day of month
        dateTo.setValue(now.withDayOfMonth(now.lengthOfMonth())); // Last day of month

        // Add listeners
        dateFrom.setOnAction(e -> updateReportPeriod());
        dateTo.setOnAction(e -> updateReportPeriod());

        updateReportPeriod();
    }

    private void setupTable() {
        colEmployeeName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployeeName()));
        colPosition.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPosition()));
        colBaseSalary.setCellValueFactory(cellData ->
                new SimpleStringProperty("Rp " + String.format("%,d", cellData.getValue().getBaseSalary())));
        colAttendanceCount.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAttendanceCount()).asObject());
        colAttendanceRate.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getAttendanceRate())));
        colAttendanceBonus.setCellValueFactory(cellData ->
                new SimpleStringProperty("Rp " + String.format("%,d", cellData.getValue().getAttendanceBonus())));
        colTotalSalary.setCellValueFactory(cellData ->
                new SimpleStringProperty("Rp " + String.format("%,d", cellData.getValue().getTotalSalary())));

        tableEmployeeReport.setItems(reportData);
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();

            ObservableList<String> employeeOptions = FXCollections.observableArrayList();
            employeeOptions.add("All Employees");

            for (Employee emp : employees) {
                employeeOptions.add(emp.getName() + " (" + emp.getPosition() + ")");
            }

            cmbEmployeeFilter.setItems(employeeOptions);
            cmbEmployeeFilter.setValue("All Employees");

            updateSummary(employees.size());

        } catch (Exception e) {
            showError("Error loading employees: " + e.getMessage());
        }
    }

    private void setDefaultValues() {
        txtBonusPerAttendance.setText("50000");
        txtWorkingDays.setText("22");
        updateReportPeriod();
    }

    private void updateReportPeriod() {
        if (dateFrom.getValue() != null && dateTo.getValue() != null) {
            String period = dateFrom.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - " + dateTo.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            lblReportPeriod.setText(period);

            long workingDays = ChronoUnit.DAYS.between(dateFrom.getValue(), dateTo.getValue()) + 1;
            lblTotalWorkingDays.setText(String.valueOf(workingDays));
        }
    }

    private void updateSummary(int totalEmployees) {
        lblTotalEmployees.setText(String.valueOf(totalEmployees));
    }

    @FXML
    private void handlePreviewReport() {
        try {
            showStatus("Generating preview...", "info");

            if (!validateInputs()) {
                return;
            }

            List<EmployeeReportData> data = generateReportData();
            reportData.clear();
            reportData.addAll(data);

            // Update summary
            updateSummary(data.size());
            if (!data.isEmpty()) {
                double avgRate = data.stream()
                        .mapToDouble(EmployeeReportData::getAttendanceRate)
                        .average()
                        .orElse(0.0);
                lblAvgAttendanceRate.setText(String.format("%.1f%%", avgRate));
            }

            showStatus("Preview generated successfully. " + data.size() + " employees loaded.", "success");

        } catch (Exception e) {
            showError("Error generating preview: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        try {
            showStatus("Membuat laporan...", "info");

            if (!validateInputs()) {
                return;
            }

            // Generate data if not already previewed
            if (reportData.isEmpty()) {
                handlePreviewReport();
            }

            // Let user choose save location
            FileChooser fileChooser = new FileChooser();

            if (rbExcel.isSelected()) {
                fileChooser.setTitle("Simpan Laporan Excel");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("File Excel", "*.xlsx")
                );
                fileChooser.setInitialFileName("Laporan_Karyawan_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

                Stage stage = (Stage) btnGenerateReport.getScene().getWindow();
                java.io.File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    generateExcelReport(file.getAbsolutePath());
                    showStatus("Laporan Excel berhasil dibuat: " + file.getName(), "success");
                }

            } else if (rbPDF.isSelected()) {
                fileChooser.setTitle("Simpan Laporan PDF");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("File PDF", "*.pdf")
                );
                fileChooser.setInitialFileName("Laporan_Karyawan_" +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

                Stage stage = (Stage) btnGenerateReport.getScene().getWindow();
                java.io.File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    generatePDFReport(file.getAbsolutePath());
                    showStatus("Laporan PDF berhasil dibuat: " + file.getName(), "success");
                }
            }

        } catch (Exception e) {
            showError("Error membuat laporan: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            showError("Please select date range");
            return false;
        }

        if (dateFrom.getValue().isAfter(dateTo.getValue())) {
            showError("From date must be before To date");
            return false;
        }

        try {
            int bonus = Integer.parseInt(txtBonusPerAttendance.getText());
            if (bonus < 0) {
                showError("Bonus per attendance must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid bonus amount");
            return false;
        }

        try {
            int workingDays = Integer.parseInt(txtWorkingDays.getText());
            if (workingDays <= 0) {
                showError("Working days must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid working days");
            return false;
        }

        return true;
    }

    private List<EmployeeReportData> generateReportData() {
        List<EmployeeReportData> data = new ArrayList<>();

        try {
            List<Employee> employees = employeeService.getAllEmployees();
            String selectedEmployee = cmbEmployeeFilter.getValue();

            // Filter employees if specific employee selected
            if (!"All Employees".equals(selectedEmployee)) {
                employees = employees.stream()
                        .filter(emp -> selectedEmployee.contains(emp.getName()))
                        .collect(Collectors.toList());
            }

            int bonusPerAttendance = Integer.parseInt(txtBonusPerAttendance.getText());
            int totalWorkingDays = Integer.parseInt(txtWorkingDays.getText());

            for (Employee employee : employees) {
                EmployeeReportData reportItem = new EmployeeReportData();
                reportItem.setEmployeeName(employee.getName());
                reportItem.setPosition(employee.getPosition());
                reportItem.setBaseSalary(employee.getSalary());

                // Get attendance count for the period
                int attendanceCount = getAttendanceCountForPeriod(employee.getEmployeeId(),
                        dateFrom.getValue(), dateTo.getValue());
                reportItem.setAttendanceCount(attendanceCount);

                // Calculate attendance rate
                double attendanceRate = (double) attendanceCount / totalWorkingDays * 100;
                reportItem.setAttendanceRate(attendanceRate);

                // Calculate attendance bonus
                int attendanceBonus = attendanceCount * bonusPerAttendance;
                reportItem.setAttendanceBonus(attendanceBonus);

                // Calculate total salary
                int totalSalary = employee.getSalary() + attendanceBonus;
                reportItem.setTotalSalary(totalSalary);

                data.add(reportItem);
            }

        } catch (Exception e) {
            System.err.println("Error generating report data: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    private int getAttendanceCountForPeriod(int employeeId, LocalDate fromDate, LocalDate toDate) {
        try {
            List<Attendance> allAttendance = employeeService.getEmployeeAttendance(employeeId);

            return (int) allAttendance.stream()
                    .filter(attendance -> {
                        LocalDate attendanceDate = attendance.getDate().toLocalDate();
                        return !attendanceDate.isBefore(fromDate) && !attendanceDate.isAfter(toDate);
                    })
                    .count();

        } catch (Exception e) {
            System.err.println("Error getting attendance count: " + e.getMessage());
            return 0;
        }
    }

    private void generateExcelReport(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Laporan Karyawan");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);

        // Create header row dengan bahasa Indonesia
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nama Karyawan", "Posisi", "Gaji Pokok", "Jumlah Kehadiran",
                "Tingkat Kehadiran (%)", "Bonus Kehadiran", "Total Gaji"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        int rowNum = 1;
        for (EmployeeReportData data : reportData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.getEmployeeName());
            row.createCell(1).setCellValue(data.getPosition());
            row.createCell(2).setCellValue(data.getBaseSalary());
            row.createCell(3).setCellValue(data.getAttendanceCount());
            row.createCell(4).setCellValue(data.getAttendanceRate());
            row.createCell(5).setCellValue(data.getAttendanceBonus());
            row.createCell(6).setCellValue(data.getTotalSalary());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }

    private void generatePDFReport(String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate()); // Landscape for table
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Title dalam bahasa Indonesia
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph("Laporan Kehadiran & Gaji Karyawan", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Report Period dalam bahasa Indonesia
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Paragraph period = new Paragraph("Periode Laporan: " + lblReportPeriod.getText(), normalFont);
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(10);
        document.add(period);

        // Generation Date dalam bahasa Indonesia
        Paragraph generatedDate = new Paragraph("Dibuat pada: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont);
        generatedDate.setAlignment(Element.ALIGN_CENTER);
        generatedDate.setSpacingAfter(20);
        document.add(generatedDate);

        // Summary Information dalam bahasa Indonesia
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(80);
        summaryTable.setSpacingAfter(20);

        // Summary headers dalam bahasa Indonesia
        PdfPCell summaryHeader1 = new PdfPCell(new Phrase("Total Karyawan", boldFont));
        PdfPCell summaryHeader2 = new PdfPCell(new Phrase("Hari Kerja", boldFont));
        PdfPCell summaryHeader3 = new PdfPCell(new Phrase("Rata-rata Kehadiran", boldFont));
        PdfPCell summaryHeader4 = new PdfPCell(new Phrase("Total Bonus Dibayar", boldFont));

        summaryHeader1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        summaryHeader2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        summaryHeader3.setBackgroundColor(BaseColor.LIGHT_GRAY);
        summaryHeader4.setBackgroundColor(BaseColor.LIGHT_GRAY);

        summaryTable.addCell(summaryHeader1);
        summaryTable.addCell(summaryHeader2);
        summaryTable.addCell(summaryHeader3);
        summaryTable.addCell(summaryHeader4);

        // Summary values
        int totalBonus = reportData.stream().mapToInt(EmployeeReportData::getAttendanceBonus).sum();

        summaryTable.addCell(new PdfPCell(new Phrase(lblTotalEmployees.getText(), normalFont)));
        summaryTable.addCell(new PdfPCell(new Phrase(lblTotalWorkingDays.getText(), normalFont)));
        summaryTable.addCell(new PdfPCell(new Phrase(lblAvgAttendanceRate.getText(), normalFont)));
        summaryTable.addCell(new PdfPCell(new Phrase("Rp " + String.format("%,d", totalBonus), normalFont)));

        document.add(summaryTable);

        // Main Data Table dengan header bahasa Indonesia
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        // Set column widths
        float[] columnWidths = {20f, 15f, 15f, 10f, 10f, 15f, 15f};
        table.setWidths(columnWidths);

        // Table headers dalam bahasa Indonesia
        String[] headers = {"Nama Karyawan", "Posisi", "Gaji Pokok", "Kehadiran",
                "Tingkat %", "Bonus Kehadiran", "Total Gaji"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Table data
        for (EmployeeReportData data : reportData) {
            table.addCell(new PdfPCell(new Phrase(data.getEmployeeName(), normalFont)));
            table.addCell(new PdfPCell(new Phrase(data.getPosition(), normalFont)));

            PdfPCell baseSalaryCell = new PdfPCell(new Phrase("Rp " + String.format("%,d", data.getBaseSalary()), normalFont));
            baseSalaryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(baseSalaryCell);

            PdfPCell attendanceCell = new PdfPCell(new Phrase(String.valueOf(data.getAttendanceCount()), normalFont));
            attendanceCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(attendanceCell);

            PdfPCell rateCell = new PdfPCell(new Phrase(String.format("%.1f%%", data.getAttendanceRate()), normalFont));
            rateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(rateCell);

            PdfPCell bonusCell = new PdfPCell(new Phrase("Rp " + String.format("%,d", data.getAttendanceBonus()), normalFont));
            bonusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(bonusCell);

            PdfPCell totalCell = new PdfPCell(new Phrase("Rp " + String.format("%,d", data.getTotalSalary()), normalFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
        }

        document.add(table);

        // Footer dalam bahasa Indonesia
        Paragraph footer = new Paragraph("\n\nDibuat oleh Sistem Manajemen Museum SeniMatic",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        writer.close();
    }

    private void showStatus(String message, String type) {
        lblGenerationStatus.setText(message);
        lblReportStatus.setText(message);

        switch (type) {
            case "success":
                lblGenerationStatus.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6;");
                break;
            case "error":
                lblGenerationStatus.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8;");
                break;
            case "info":
                lblGenerationStatus.setStyle("-fx-text-fill: #3498db; -fx-background-color: #d6eaf8;");
                break;
            default:
                lblGenerationStatus.setStyle("-fx-text-fill: #2c3e50; -fx-background-color: #f8f9fa;");
        }
    }

    private void showError(String message) {
        showStatus("Error: " + message, "error");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Report Generation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for table data
    public static class EmployeeReportData {
        private String employeeName;
        private String position;
        private int baseSalary;
        private int attendanceCount;
        private double attendanceRate;
        private int attendanceBonus;
        private int totalSalary;

        // Getters and setters
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public int getBaseSalary() { return baseSalary; }
        public void setBaseSalary(int baseSalary) { this.baseSalary = baseSalary; }

        public int getAttendanceCount() { return attendanceCount; }
        public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }

        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }

        public int getAttendanceBonus() { return attendanceBonus; }
        public void setAttendanceBonus(int attendanceBonus) { this.attendanceBonus = attendanceBonus; }

        public int getTotalSalary() { return totalSalary; }
        public void setTotalSalary(int totalSalary) { this.totalSalary = totalSalary; }
    }
}