import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditPetForm extends JDialog {
    private int petId;
    private JTextField nameField, typeField, breedField, ageField, genderField, vaccineField, disField, locField, imgField;

    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
    static final String DB_USER = "petapp";
    static final String DB_PASS = "pet123";

    // 🟩 Constructor
    public EditPetForm(JFrame owner, int petId) {
        super(owner, "Edit Pet Details", true); // modal dialog
        this.petId = petId;

        setSize(500, 520);
        setLayout(new GridLayout(11, 2, 10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(57, 89, 142));

        // 🔹 Labels
        JLabel[] labels = {
            new JLabel("Name:"), new JLabel("Animal Type:"), new JLabel("Breed:"),
            new JLabel("Age:"), new JLabel("Gender:"), new JLabel("Last Vaccine:"),
            new JLabel("Disabilities:"), new JLabel("Location:"), new JLabel("Image URLs:")
        };

        for (JLabel lbl : labels) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        // 🔹 Text fields
        nameField = new JTextField();
        typeField = new JTextField();
        breedField = new JTextField();
        ageField = new JTextField();
        genderField = new JTextField();
        vaccineField = new JTextField();
        disField = new JTextField();
        locField = new JTextField();
        imgField = new JTextField();

        // 🔹 Add labels and fields
        add(labels[0]); add(nameField);
        add(labels[1]); add(typeField);
        add(labels[2]); add(breedField);
        add(labels[3]); add(ageField);
        add(labels[4]); add(genderField);
        add(labels[5]); add(vaccineField);
        add(labels[6]); add(disField);
        add(labels[7]); add(locField);
        add(labels[8]); add(imgField);

        // 🔹 Buttons
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(72, 201, 176));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.addActionListener(e -> updatePet());

        JButton deleteBtn = new JButton("Delete Pet");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteBtn.addActionListener(e -> deletePet());

        add(saveBtn);
        add(deleteBtn);

        // 🔹 Finally, load the existing pet data
        loadPetData();
    }

    // 🟩 Load existing pet data into fields
    private void loadPetData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM pets WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, petId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                typeField.setText(rs.getString("animal_type"));
                breedField.setText(rs.getString("breed"));
                ageField.setText(rs.getString("age"));
                genderField.setText(rs.getString("gender"));
                vaccineField.setText(rs.getString("last_vaccine"));
                disField.setText(rs.getString("disabilities"));
                locField.setText(rs.getString("location"));
                imgField.setText(rs.getString("image_url"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    // 🟩 Save updates to the database
    private void updatePet() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "UPDATE pets SET name=?, animal_type=?, breed=?, age=?, gender=?, last_vaccine=?, disabilities=?, location=?, image_url=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.setString(2, typeField.getText());
            ps.setString(3, breedField.getText());
            ps.setString(4, ageField.getText());
            ps.setString(5, genderField.getText());
            ps.setString(6, vaccineField.getText());
            ps.setString(7, disField.getText());
            ps.setString(8, locField.getText());
            ps.setString(9, imgField.getText());
            ps.setInt(10, petId);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Pet details updated successfully!");
            dispose(); // ✅ close after saving
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    // 🟩 Delete pet from database
    private void deletePet() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this pet?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String deleteRequests = "DELETE FROM adoption_requests WHERE pet_id = ?";
                PreparedStatement ps2 = conn.prepareStatement(deleteRequests);
                ps2.setInt(1, petId);
                ps2.executeUpdate();

                String sql = "DELETE FROM pets WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, petId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "🐾 Pet deleted successfully!");
                dispose(); // ✅ close after deleting
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }
    }
}
