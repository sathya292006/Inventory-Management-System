import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SalesForm extends JFrame {
    private JTextField txtBarcode;
    private JTextField txtQuantity;
    private JFrame parent;

    public SalesForm(JFrame parent) {
        this.parent = parent;
        setTitle("Record Sales");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15,15,15,15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblBarcode = new JLabel("Product Barcode:");
        lblBarcode.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        add(lblBarcode, gbc);

        txtBarcode = new JTextField(20);
        txtBarcode.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 0;
        add(txtBarcode, gbc);

        JLabel lblQuantity = new JLabel("Quantity Sold:");
        lblQuantity.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 1;
        add(lblQuantity, gbc);

        txtQuantity = new JTextField(20);
        txtQuantity.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 1;
        add(txtQuantity, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnAdd = new JButton("Add Sale");
        btnAdd.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnAdd.addActionListener(e -> addSale());
        btnPanel.add(btnAdd);

        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnBack.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });
        btnPanel.add(btnBack);

        JButton btnNext = new JButton("Next (Reports)");
        btnNext.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnNext.addActionListener(e -> {
            new ReportsForm(parent).setVisible(true);
            dispose();
        });
        btnPanel.add(btnNext);

        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnExit.addActionListener(e -> System.exit(0));
        btnPanel.add(btnExit);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(btnPanel, gbc);
    }

    private void addSale() {
        String barcode = txtBarcode.getText().trim();
        String quantityStr = txtQuantity.getText().trim();

        if (barcode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter product barcode", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if product exists and has sufficient quantity
            PreparedStatement psCheck = conn.prepareStatement("SELECT Quantity FROM Products WHERE Barcode=?");
            psCheck.setString(1, barcode);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int availableQty = rs.getInt("Quantity");
            if (availableQty < quantity) {
                JOptionPane.showMessageDialog(this, "Insufficient stock. Available: " + availableQty, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert into Sales
            PreparedStatement psSale = conn.prepareStatement("INSERT INTO Sales (Barcode, QuantitySold) VALUES (?, ?)");
            psSale.setString(1, barcode);
            psSale.setInt(2, quantity);
            psSale.executeUpdate();

            // Update Products Quantity
            PreparedStatement psUpdate = conn.prepareStatement("UPDATE Products SET Quantity=Quantity-? WHERE Barcode=?");
            psUpdate.setInt(1, quantity);
            psUpdate.setString(2, barcode);
            psUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Sale recorded successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
