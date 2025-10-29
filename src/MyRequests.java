import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MyRequests extends JFrame {
    JTable table;
    DefaultTableModel model;
    JTextArea messageBox;

    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
    static final String DB_USER = "petapp";
    static final String DB_PASS = "pet123";

    public MyRequests(String adopterName, String adopterPhone, JFrame owner) {
        setTitle("AdoptEase - My Adoption Requests");
        setSize(850, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔹 Table setup
        model = new DefaultTableModel(new String[]{
                "Request ID", "Pet Name", "Adopter", "Status"
        }, 0);

        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(30);

        // 🎨 Add color to status
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if (status.equalsIgnoreCase("Pending")) {
                    c.setForeground(new Color(230, 126, 34)); // orange
                } else if (status.equalsIgnoreCase("Approved")) {
                    c.setForeground(new Color(39, 174, 96)); // green
                } else if (status.equalsIgnoreCase("Rejected")) {
                    c.setForeground(new Color(231, 76, 60)); // red
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(renderer);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 🔹 Message box below
        messageBox = new JTextArea();
        messageBox.setEditable(false);
        messageBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageBox.setLineWrap(true);
        messageBox.setWrapStyleWord(true);
        messageBox.setBorder(BorderFactory.createTitledBorder("Status Message"));
        add(new JScrollPane(messageBox), BorderLayout.SOUTH);

        // ✅ Only show if there are requests
        if (loadRequests(adopterName, adopterPhone)) {
            // Show the window only if data exists
            setVisible(true);
        } else {
            // Close if no data, just show popup
            dispose();
        }

        // 🔹 When user clicks a row, show message
        table.getSelectionModel().addListSelectionListener(e -> showMessageForSelectedRow());
    }

    private boolean loadRequests(String adopterName, String adopterPhone) {
        model.setRowCount(0);
        messageBox.setText("");

        String sql = "SELECT ar.id, p.name AS pet_name, ar.adopter_name, ar.status " +
                     "FROM adoption_requests ar JOIN pets p ON ar.pet_id = p.id " +
                     "WHERE ar.adopter_name = ? AND ar.adopter_phone = ?";

        boolean hasRequests = false;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, adopterName);
            ps.setString(2, adopterPhone);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                hasRequests = true;
                String petName = rs.getString("pet_name");
                String adopter = rs.getString("adopter_name");
                String status = rs.getString("status");

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        petName,
                        adopter,
                        status
                });

                // 🎨 Add message for this pet based on status
                if (status.equalsIgnoreCase("Approved")) {
                    messageBox.append("✅ Your adoption request for " + petName + " has been approved!\n");
                } else if (status.equalsIgnoreCase("Rejected")) {
                    messageBox.append("❌ Your adoption request for " + petName + " has been rejected.\n");
                } else if (status.equalsIgnoreCase("Pending")) {
                    messageBox.append("⏳ Your adoption request for " + petName + " is still pending.\n");
                }
            }

            if (!hasRequests) {
                JOptionPane.showMessageDialog(this, "No adoption requests found for this name and phone number.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }

        return hasRequests;
    }

    private void showMessageForSelectedRow() {
        int selected = table.getSelectedRow();
        if (selected == -1) return;

        String petName = model.getValueAt(selected, 1).toString();
        String status = model.getValueAt(selected, 3).toString();

        if (status.equalsIgnoreCase("Pending")) {
            messageBox.setText("⏳ Your request to adopt " + petName + " is still pending. Please wait for the owner's response.");
        } else if (status.equalsIgnoreCase("Approved")) {
            messageBox.setText("🎉 Congratulations! Your request to adopt " + petName + " has been approved! The owner will contact you soon.");
        } else if (status.equalsIgnoreCase("Rejected")) {
            messageBox.setText("😔 We’re sorry, but your request to adopt " + petName + " has been rejected by the owner.");
        } else {
            messageBox.setText("Unknown status for " + petName);
        }
    }
}
