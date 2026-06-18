
import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    public MainMenu(boolean isAdmin) {
        setTitle("Main Menu - Inventory System");
        setSize(600, 500); // Increased size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main Panel with BoxLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // Padding

        // Button Styles
        Font btnFont = new Font("Tahoma", Font.BOLD, 24);
        Dimension btnSize = new Dimension(400, 60);

        // Manage Products Button
        JButton btnProducts = createMenuButton("Manage Products", btnFont, btnSize);
        btnProducts.addActionListener(e -> openForm(new ProductForm(this)));

        // Record Sales Button
        JButton btnSales = createMenuButton("Record Sales", btnFont, btnSize);
        btnSales.addActionListener(e -> openForm(new SalesForm(this)));

        // Generate Reports Button
        JButton btnReports = createMenuButton("Generate Reports", btnFont, btnSize);
        btnReports.addActionListener(e -> openForm(new ReportsForm(this)));

        // Manage Users Button (Admin Only)
        JButton btnUsers = createMenuButton("Manage Users", btnFont, btnSize);
        btnUsers.setEnabled(isAdmin);
        btnUsers.addActionListener(e -> openForm(new UsersForm(this)));

        // Exit Button
        JButton btnExit = createMenuButton("Exit", btnFont, btnSize);
        btnExit.addActionListener(e -> System.exit(0));

        // Add Components with Spacing
        mainPanel.add(btnProducts);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacer
        mainPanel.add(btnSales);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacer
        mainPanel.add(btnReports);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25))); // Spacer
        mainPanel.add(btnUsers);
        mainPanel.add(Box.createVerticalGlue()); // Pushes exit button to bottom
        mainPanel.add(btnExit);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // Helper method to create styled buttons
    private JButton createMenuButton(String text, Font font, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // Helper method to open forms
    private void openForm(JFrame form) {
        form.setVisible(true);
        setVisible(false);
    }
}