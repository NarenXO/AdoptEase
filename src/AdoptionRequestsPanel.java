import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdoptionRequestsPanel extends JFrame {
    JTable table;
    DefaultTableModel model;

    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
    static final String DB_USER = "petapp";
    static final String DB_PASS = "pet123";

    public AdoptionRequestsPanel(JFrame owner) {
        setTitle("AdoptEase - Adoption Requests");
        setSize(900, 500);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        model = new DefaultTableModel(new String[]{
                "Request ID", "Pet ID", "Adopter Name", "Age", "Gender",
                "Location", "Occupation", "Phone", "Status"
        }, 0);
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");

        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);

        approveBtn.setBackground(new Color(39, 174, 96));
        approveBtn.setForeground(Color.WHITE);

        rejectBtn.setBackground(new Color(231, 76, 60));
        rejectBtn.setForeground(Color.WHITE);

        btnPanel.add(refreshBtn);
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Button actions
        refreshBtn.addActionListener(e -> loadRequests());
        approveBtn.addActionListener(e -> updateStatus("Approved"));
        rejectBtn.addActionListener(e -> updateStatus("Rejected"));

        boolean hasRequests = loadRequests();

if (!hasRequests) {
    JOptionPane.showMessageDialog(owner,
            "No adoption requests yet 🐾",
            "AdoptEase",
            JOptionPane.INFORMATION_MESSAGE);
    dispose();
    return;
}

setVisible(true);
    }

  private boolean loadRequests() {
    model.setRowCount(0);
    boolean hasRequests = false;

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM adoption_requests WHERE status = 'Pending'")) {

        while (rs.next()) {
            hasRequests = true;
            model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("pet_id"),
                    rs.getString("adopter_name"),
                    rs.getString("adopter_age"),
                    rs.getString("adopter_gender"),
                    rs.getString("adopter_location"),
                    rs.getString("adopter_occupation"),
                    rs.getString("adopter_phone"),
                    rs.getString("status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
    }

    return hasRequests;
}



    private void updateStatus(String newStatus) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        int requestId = (int) model.getValueAt(selected, 0);
        String adopterName = (String) model.getValueAt(selected, 2);
        String adopterPhone = (String) model.getValueAt(selected, 7);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("UPDATE adoption_requests SET status=? WHERE id=?")) {

            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            ps.executeUpdate();

            if (newStatus.equalsIgnoreCase("Approved")) {
    // mark the pet as adopted
    try (PreparedStatement ps2 = conn.prepareStatement("UPDATE pets SET is_adopted = TRUE WHERE id = (SELECT pet_id FROM adoption_requests WHERE id = ?)")) {
        ps2.setInt(1, requestId);
        ps2.executeUpdate();
    }
}


            JOptionPane.showMessageDialog(this,
                    "📩 " + adopterName + " (" + adopterPhone + ") has been informed.\nRequest marked as " + newStatus + ".");
            model.removeRow(selected);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
}
