import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ListPetForm extends JFrame {
    private HomePage owner;
    public void setOwner(HomePage owner) {
        this.owner = owner;
    }
    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
    static final String DB_USER = "root";
    static final String DB_PASS = "1234"; // change if your MySQL password is different

   private JTextField nameField, ageField, genderField, vaccineField, disField, locField, imgField, breedField;
private JTextField ownerNameField, ownerPhoneField;

   private JTextField animalTypeField;


    public ListPetForm(JFrame owner) {
        setTitle("List a Pet for Adoption");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(12, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.setBackground(new Color(57, 89, 142));

       JLabel[] labels = {
    new JLabel("Pet Name:"), new JLabel("Animal Type:"), new JLabel("Breed:"),
    new JLabel("Age:"), new JLabel("Gender:"), new JLabel("Last Vaccine:"),
    new JLabel("Disabilities:"), new JLabel("Location:"), new JLabel("Select Image:")
};


        for (JLabel lbl : labels) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        nameField = new JTextField();
        animalTypeField = new JTextField();

        breedField = new JTextField();
        ageField = new JTextField();
        genderField = new JTextField();
        vaccineField = new JTextField();
        disField = new JTextField();
        locField = new JTextField();
        imgField = new JTextField();

        JButton uploadBtn = new JButton("Choose File");
uploadBtn.setBackground(new Color(52, 152, 219));
uploadBtn.setForeground(Color.WHITE);
uploadBtn.addActionListener(e -> chooseImage());

   
        JButton submitBtn = new JButton("Submit");
        submitBtn.setBackground(new Color(46, 204, 113));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.addActionListener(e -> addPetToDB());

        panel.add(labels[0]); panel.add(nameField);
        panel.add(labels[1]); panel.add(animalTypeField);
        panel.add(labels[2]); panel.add(breedField);
        panel.add(labels[3]); panel.add(ageField);
        panel.add(labels[4]); panel.add(genderField);
        panel.add(labels[5]); panel.add(vaccineField);
        panel.add(labels[6]); panel.add(disField);
        panel.add(labels[7]); panel.add(locField);
       panel.add(labels[8]);
JPanel imgPanel = new JPanel(new BorderLayout());
imgPanel.setBackground(new Color(57, 89, 142));
imgPanel.add(imgField, BorderLayout.CENTER);
imgPanel.add(uploadBtn, BorderLayout.EAST);
panel.add(imgPanel);

       // Create new fields
ownerNameField = new JTextField();
ownerPhoneField = new JTextField();

// Add Owner fields to the panel
JLabel ownerNameLbl = new JLabel("Owner Name:");
ownerNameLbl.setForeground(Color.WHITE);
ownerNameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

JLabel ownerPhoneLbl = new JLabel("Owner Phone:");
ownerPhoneLbl.setForeground(Color.WHITE);
ownerPhoneLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

panel.add(ownerNameLbl); panel.add(ownerNameField);
panel.add(ownerPhoneLbl); panel.add(ownerPhoneField);


        panel.add(new JLabel("")); panel.add(submitBtn);

        add(panel, BorderLayout.CENTER);
    }

    private void addPetToDB() {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
        conn.setAutoCommit(false); // make both inserts atomic

        String ownerName = ownerNameField.getText().trim();
        String ownerPhone = ownerPhoneField.getText().trim();

        // Check if owner already exists
        String findOwnerSQL = "SELECT id FROM owners WHERE phone = ?";
        PreparedStatement findOwnerStmt = conn.prepareStatement(findOwnerSQL);
        findOwnerStmt.setString(1, ownerPhone);
        ResultSet rs = findOwnerStmt.executeQuery();

        int ownerId;
        if (rs.next()) {
            ownerId = rs.getInt("id");
        } else {
            // Insert new owner
            String insertOwnerSQL = "INSERT INTO owners (name, phone) VALUES (?, ?)";
            PreparedStatement insertOwnerStmt = conn.prepareStatement(insertOwnerSQL, Statement.RETURN_GENERATED_KEYS);
            insertOwnerStmt.setString(1, ownerName);
            insertOwnerStmt.setString(2, ownerPhone);
            insertOwnerStmt.executeUpdate();

            ResultSet keys = insertOwnerStmt.getGeneratedKeys();
            keys.next();
            ownerId = keys.getInt(1);
        }

        // Insert pet linked with owner_id
        String sql = "INSERT INTO pets (name, animal_type, breed, age, gender, last_vaccine, disabilities, location, image_url, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nameField.getText());
        ps.setString(2, animalTypeField.getText());
        ps.setString(3, breedField.getText());
        ps.setString(4, ageField.getText());
        ps.setString(5, genderField.getText());
        ps.setString(6, vaccineField.getText());
        ps.setString(7, disField.getText());
        ps.setString(8, locField.getText());
        ps.setString(9, imgField.getText());
        ps.setInt(10, ownerId);

        ps.executeUpdate();
        conn.commit();

        JOptionPane.showMessageDialog(this, "✅ Pet added successfully!");
        if (getOwner() instanceof HomePage) {
            ((HomePage) getOwner()).refreshPets();
        }
        dispose(); // auto-close window

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ListPetForm(null).setVisible(true));
    }

    private void chooseImage() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        java.io.File file = chooser.getSelectedFile();
        imgField.setText(file.getAbsolutePath()); // store full file path
    }
}


}
