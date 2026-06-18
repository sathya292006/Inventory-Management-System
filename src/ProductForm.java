import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtBarcode, txtName, txtPrice, txtQuantity, txtCategory;
    private JFrame parent;

    public ProductForm(JFrame parent) {
        this.parent = parent;
        setTitle("Manage Products");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"Barcode", "Name", "Price", "Quantity", "Category"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 16));
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        txtBarcode = new JTextField();
        txtBarcode.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtName = new JTextField();
        txtName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtPrice = new JTextField();
        txtPrice.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtQuantity = new JTextField();
        txtQuantity.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtCategory = new JTextField();
        txtCategory.setFont(new Font("Tahoma", Font.PLAIN, 16));

        inputPanel.add(new JLabel("Barcode:"));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(new JLabel("Category:"));

        inputPanel.add(txtBarcode);
        inputPanel.add(txtName);
        inputPanel.add(txtPrice);
        inputPanel.add(txtQuantity);
        inputPanel.add(txtCategory);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        JButton btnAdd = new JButton("Add");
        btnAdd.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnAdd.addActionListener(e -> addProduct());

        JButton btnDelete = new JButton("Delete");
        btnDelete.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnDelete.addActionListener(e -> deleteProduct());

        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnBack.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });

        JButton btnNext = new JButton("Next (Sales)");
        btnNext.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnNext.addActionListener(e -> {
            new SalesForm(parent).setVisible(true);
            dispose();
        });

        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnExit.addActionListener(e -> System.exit(0));

        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnBack);
        btnPanel.add(btnNext);
        btnPanel.add(btnExit);

        add(scroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.SOUTH);

        loadProducts();
    }

    private void loadProducts() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Products");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("Barcode"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getInt("Quantity"),
                        rs.getString("Category")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        String barcode = txtBarcode.getText().trim();
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        String category = txtCategory.getText().trim();

        if (barcode.isEmpty() || name.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Products (Barcode, Name, Price, Quantity, Category) VALUES (?, ?, ?, ?, ?)"
                );
                ps.setString(1, barcode);
                ps.setString(2, name);
                ps.setDouble(3, price);
                ps.setInt(4, quantity);
                ps.setString(5, category);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Added!");
                loadProducts();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price and Quantity must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String barcode = (String) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete product with barcode: " + barcode + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM Products WHERE Barcode=?");
                ps.setString(1, barcode);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product Deleted!");
                loadProducts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}