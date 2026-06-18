import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UsersForm extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JFrame parent;

    public UsersForm(JFrame parent) {
        this.parent = parent;
        setTitle("Manage Users");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"User ID", "Username", "Role"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 16));
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 16));
        cmbRole = new JComboBox<>(new String[]{"Admin", "Staff"});
        cmbRole.setFont(new Font("Tahoma", Font.BOLD, 16));

        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(txtUsername);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(txtPassword);
        inputPanel.add(new JLabel("Role:"));
        inputPanel.add(cmbRole);

        JButton btnAdd = new JButton("Add User");
        btnAdd.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnAdd.addActionListener(e -> addUser());
        inputPanel.add(btnAdd);

        JButton btnDelete = new JButton("Delete User");
        btnDelete.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnDelete.addActionListener(e -> deleteUser());
        inputPanel.add(btnDelete);

        add(inputPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnBack.addActionListener(e -> {
            parent.setVisible(true);
            dispose();
        });

        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnExit.addActionListener(e -> System.exit(0));

        btnPanel.add(btnBack);
        btnPanel.add(btnExit);

        add(btnPanel, BorderLayout.SOUTH);

        loadUsers();
    }

    private void loadUsers() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT UserID, Username, Role FROM Users");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Role")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addUser() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String role = (String) cmbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill username and password", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Users (Username, Password, Role) VALUES (?, SHA2(?, 256), ?)"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User added!");
            loadUsers();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to delete", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user ID: " + userId + " ?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE UserID=?");
                ps.setInt(1, userId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted!");
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
