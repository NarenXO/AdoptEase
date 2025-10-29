import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdoptionRequestForm extends JDialog {
    private int petId;
    private JTextField nameField, ageField, genderField, locationField, occupationField, phoneField;

    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
    static final String DB_USER = "petapp";
    static final String DB_PASS = "pet123";

    // 🟩 Constructor
    public AdoptionRequestForm(JFrame owner, int petId) {
        super(owner, "Adoption Request", true); // modal JDialog
        this.petId = petId;

        setSize(400, 400);
        setLayout(new GridLayout(8, 2, 10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        // 🔹 Fields
        nameField = new JTextField();
        ageField = new JTextField();
        genderField = new JTextField();
        locationField = new JTextField();
        occupationField = new JTextField();
        phoneField = new JTextField();

        // 🔹 Add components
        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Age:"));
        add(ageField);
        add(new JLabel("Gender:"));
        add(genderField);
        add(new JLabel("Location:"));
        add(locationField);
        add(new JLabel("Occupation:"));
        add(occupationField);
        add(new JLabel("Phone:"));
        add(phoneField);

        JButton submitBtn = new JButton("Submit Request");
        submitBtn.setBackground(new Color(72, 201, 176));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.addActionListener(e -> submitRequest());
        add(new JLabel()); // spacer
        add(submitBtn);
    }

    // 🟩 Submit method
   private void submitRequest() {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        String sql = "INSERT INTO adoption_requests (pet_id, adopter_name, adopter_age, adopter_gender, adopter_location, adopter_occupation, adopter_phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, petId);
        ps.setString(2, nameField.getText());
        ps.setString(3, ageField.getText());
        ps.setString(4, genderField.getText());
        ps.setString(5, locationField.getText());
        ps.setString(6, occupationField.getText());
        ps.setString(7, phoneField.getText());
        ps.executeUpdate();

        JOptionPane.showMessageDialog(this, "✅ Adoption request sent successfully!");

        // ✅ Update adopter info in HomePage
        if (getOwner() instanceof HomePage) {
            HomePage home = (HomePage) getOwner();
            home.setCurrentAdopter(nameField.getText(), phoneField.getText());
        }

        // ✅ Optionally open MyRequests directly after submitting
        new MyRequests(nameField.getText(), phoneField.getText(), (JFrame) getOwner()).setVisible(true);

        dispose(); // ✅ Closes this window
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
    }
}
}
