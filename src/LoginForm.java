
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("Login - Inventory Management System");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(lblUser, gbc);

        // Username Field (Visible Text)
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 18));
        txtUsername.setPreferredSize(new Dimension(250, 40));
        txtUsername.setCaretColor(Color.BLACK);
        txtUsername.setBackground(Color.WHITE);
        txtUsername.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(txtUsername, gbc);

        // Password Label
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Tahoma", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(lblPass, gbc);

        // Password Field (Hidden Input)
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 18));
        txtPassword.setPreferredSize(new Dimension(250, 40));
        txtPassword.setEchoChar('*');
        txtPassword.setCaretColor(Color.BLACK);
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(txtPassword, gbc);

        // Login Button
        JButton btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 20));
        btnLogin.setPreferredSize(new Dimension(150, 40));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(btnLogin, gbc);

        // Action Listener
        btnLogin.addActionListener(e -> loginCheck());

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void loginCheck() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM Users WHERE Username=? AND Password=SHA2(?, 256)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtUsername.getText().trim());
            pstmt.setString(2, new String(txtPassword.getPassword()).trim());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean isAdmin = rs.getString("Role").equalsIgnoreCase("Admin");
                new MainMenu(isAdmin).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
