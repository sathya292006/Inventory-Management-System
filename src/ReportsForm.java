import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;

public class ReportsForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JFrame parent;

    public ReportsForm(JFrame parent) {
        this.parent = parent;
        setTitle("Sales Reports");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Table setup
        model = new DefaultTableModel(new String[]{"Sale ID", "Barcode", "Product Name", "Qty Sold", "Sale Date"}, 0);
        table = new JTable(model);
        table.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 14));
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Button panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnBack = new JButton("Back");
        btnBack.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        btnBack.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });

        JButton btnExportPDF = new JButton("Export to PDF");
        btnExportPDF.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 16));
        btnExportPDF.addActionListener(this::generatePDFReport);

        btnPanel.add(btnBack);
        btnPanel.add(btnExportPDF);
        add(btnPanel, BorderLayout.SOUTH);

        loadSales();
    }

    private void loadSales() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query = """
                SELECT s.SaleID, s.Barcode, p.Name, s.QuantitySold, s.SaleDate 
                FROM Sales s
                JOIN Products p ON s.Barcode = p.Barcode
                ORDER BY s.SaleDate DESC
                """;
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("SaleID"),
                        rs.getString("Barcode"),
                        rs.getString("Name"),
                        rs.getInt("QuantitySold"),
                        rs.getTimestamp("SaleDate")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePDFReport(ActionEvent e) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            String fileName = "Inventory_Report.pdf";  // fixed filename to overwrite same file
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 0, 255));
            Paragraph title = new Paragraph("INVENTORY MANAGEMENT SYSTEM REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Section 1: Sales Report
            document.add(new Paragraph("SALES TRANSACTIONS",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            addSalesTable(document);

            // Section 2: Stock Availability
            document.add(new Paragraph("\n\nCURRENT STOCK AVAILABILITY",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            addStockTable(document);

            document.close();
            JOptionPane.showMessageDialog(this, "PDF report generated: " + fileName,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSalesTable(Document document) throws Exception {
        PdfPTable pdfTable = new PdfPTable(5);
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingBefore(10f);
        pdfTable.setSpacingAfter(10f);

        // Add colorful header cells
        String[] headers = {"Sale ID", "Barcode", "Product Name", "Qty Sold", "Date"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
            headerCell.setBackgroundColor(new BaseColor(0, 121, 182)); // nice blue color
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfTable.addCell(headerCell);
        }

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("""
                SELECT s.SaleID, s.Barcode, p.Name, s.QuantitySold, s.SaleDate 
                FROM Sales s JOIN Products p ON s.Barcode = p.Barcode
                ORDER BY s.SaleDate DESC
                """);
            while (rs.next()) {
                pdfTable.addCell(String.valueOf(rs.getInt("SaleID")));
                pdfTable.addCell(rs.getString("Barcode"));
                pdfTable.addCell(rs.getString("Name"));
                pdfTable.addCell(String.valueOf(rs.getInt("QuantitySold")));
                pdfTable.addCell(rs.getTimestamp("SaleDate").toString());
            }
        }

        document.add(pdfTable);
    }

    private void addStockTable(Document document) throws Exception {
        PdfPTable pdfTable = new PdfPTable(5);
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingBefore(10f);
        pdfTable.setSpacingAfter(10f);

        // Add colorful header cells
        String[] headers = {"Barcode", "Product Name", "Price", "Qty Available", "Category"};
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
            headerCell.setBackgroundColor(new BaseColor(0, 121, 182)); // nice blue color
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfTable.addCell(headerCell);
        }

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("""
                SELECT Barcode, Name, Price, Quantity, Category 
                FROM Products 
                ORDER BY Quantity ASC
                """);
            while (rs.next()) {
                pdfTable.addCell(rs.getString("Barcode"));
                pdfTable.addCell(rs.getString("Name"));
                pdfTable.addCell(String.format("$%.2f", rs.getDouble("Price")));
                pdfTable.addCell(String.valueOf(rs.getInt("Quantity")));
                pdfTable.addCell(rs.getString("Category"));
            }
        }

        document.add(pdfTable);
    }
}
