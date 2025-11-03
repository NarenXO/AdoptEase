import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HomePage extends JFrame {


static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
static final String DB_USER = "root";
static final String DB_PASS = "1234"; // change if your MySQL password is different
private String currentAdopterName = "";
private String currentAdopterPhone = "";

public void refreshPets() {
    getContentPane().removeAll();
    new HomePage().setVisible(true);
    dispose();
}

public void setCurrentAdopter(String name, String phone) {
    this.currentAdopterName = name;
    this.currentAdopterPhone = phone;
}

public HomePage() {
    setTitle("AdoptEase");
    setSize(1000, 650);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    topPanel.setBackground(new Color(30, 39, 46));
    add(topPanel, BorderLayout.NORTH);

    JPanel background = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(38, 70, 83),
                    getWidth(), getHeight(), new Color(42, 157, 143));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    background.setLayout(new BorderLayout());
    add(background);

    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(false);

    JLabel title = new JLabel("AdoptEase", SwingConstants.CENTER);
    title.setFont(new Font("Georgia", Font.BOLD, 38));
    title.setForeground(Color.WHITE);
    header.add(title, BorderLayout.CENTER);

    JButton refreshBtn = new JButton("🔄 Refresh");
    refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    refreshBtn.setBackground(new Color(46, 134, 193));
    refreshBtn.setForeground(Color.WHITE);
    refreshBtn.addActionListener(e -> refreshPets());
    topPanel.add(refreshBtn);

    JButton donateBtn = new JButton("List a Pet for Adoption");
    styleButton(donateBtn, new Color(244, 162, 97));
    topPanel.add(donateBtn);

    JButton myReqBtn = new JButton("My Requests");
    myReqBtn.setBackground(new Color(52, 152, 219));
    myReqBtn.setForeground(Color.WHITE);
    myReqBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    myReqBtn.addActionListener(e ->
            new MyRequests(currentAdopterName, currentAdopterPhone, this));
    topPanel.add(myReqBtn);

    JButton requestsBtn = new JButton("Adoption Requests");
    requestsBtn.setBackground(new Color(230, 126, 34));
    requestsBtn.setForeground(Color.WHITE);
    requestsBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    requestsBtn.addActionListener(e -> new AdoptionRequestsPanel(this));
    topPanel.add(requestsBtn);

    JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
    topButtons.setOpaque(false);
    topButtons.add(donateBtn);
    header.add(topButtons, BorderLayout.EAST);
    background.add(header, BorderLayout.NORTH);

    title.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                String name = JOptionPane.showInputDialog(HomePage.this, "Admin name:");
                if (name == null) return;
                String pass = JOptionPane.showInputDialog(HomePage.this, "Admin password:");
                if ("admin".equals(name) && "admin123".equals(pass)) {
                    JOptionPane.showMessageDialog(HomePage.this, "Admin access granted (placeholder).");
                } else {
                    JOptionPane.showMessageDialog(HomePage.this, "Invalid admin credentials.");
                }
            }
        }
    });

    JPanel petCardFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
    petCardFlowPanel.setBackground(new Color(38, 70, 83));

    JPanel petScrollWrapper = new JPanel();
    petScrollWrapper.setLayout(new BoxLayout(petScrollWrapper, BoxLayout.Y_AXIS));
    petScrollWrapper.setBackground(new Color(38, 70, 83));
    petScrollWrapper.add(petCardFlowPanel);

    JScrollPane scrollPane = new JScrollPane(petScrollWrapper);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    background.add(scrollPane, BorderLayout.CENTER);

    loadPetsFromDatabase(petCardFlowPanel);

    JPanel footer = new JPanel();
    footer.setOpaque(false);
    JLabel footerText = new JLabel("© 2025 AdoptEase | Bringing joy, one paw at a time");
    footerText.setForeground(Color.WHITE);
    footerText.setFont(new Font("SansSerif", Font.PLAIN, 13));
    footer.add(footerText);
    background.add(footer, BorderLayout.SOUTH);

    donateBtn.addActionListener(e -> {
        ListPetForm form = new ListPetForm(this);
        form.setLocationRelativeTo(this);
        form.setVisible(true);
        form.setOwner(this);
    });
}

private void loadPetsFromDatabase(JPanel petPanel) {
    petPanel.removeAll();
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT * FROM pets WHERE is_adopted = FALSE");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String animalType = rs.getString("animal_type");
            String breed = rs.getString("breed");
            String age = rs.getString("age");
            String gender = rs.getString("gender");
            String vaccine = rs.getString("last_vaccine");
            String disability = rs.getString("disabilities");
            String location = rs.getString("location");
            String imageURLs = rs.getString("image_url");
            addPetCard(petPanel, id, name, animalType, breed, age, gender, vaccine, disability, location, imageURLs);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
    }
    petPanel.revalidate();
    petPanel.repaint();
}

// ✅ FIXED: moved your card creation code into this new method
private void addPetCard(JPanel panel, int petId, String name, String type, String breed, String age,
                        String gender, String vaccine, String disability, String location, String imageURLs) {

    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    card.setPreferredSize(new Dimension(200, 200));

    JLabel imageLabel = new JLabel("Loading...", SwingConstants.CENTER);
    imageLabel.setPreferredSize(new Dimension(180, 130));
    imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    card.add(imageLabel, BorderLayout.CENTER);

    String[] images = imageURLs != null ? imageURLs.split(",") : new String[]{};
    final String imagePath = images.length > 0 ? images[0].trim() : null;

    new SwingWorker<ImageIcon, Void>() {
        @Override
        protected ImageIcon doInBackground() throws Exception {
            if (imagePath == null || imagePath.isEmpty()) return null;
            Image image;
            if (imagePath.startsWith("http"))
                image = new ImageIcon(new java.net.URL(imagePath)).getImage();
            else
                image = new ImageIcon(imagePath).getImage();
            Image scaledImage = image.getScaledInstance(180, 130, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }

        @Override
        protected void done() {
            try {
                ImageIcon icon = get();
                if (icon != null) {
                    imageLabel.setText(null);
                    imageLabel.setIcon(icon);
                } else imageLabel.setText("No image available");
            } catch (Exception e) {
                imageLabel.setText("Image error");
            }
            panel.revalidate();
            panel.repaint();
        }
    }.execute();

    JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    nameLabel.setOpaque(true);
    nameLabel.setBackground(new Color(245, 245, 245));
    nameLabel.setForeground(Color.DARK_GRAY);
    nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    card.add(nameLabel, BorderLayout.SOUTH);

    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    card.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            showPetDetails(petId, name, type, breed, age, gender, vaccine, disability, location, images);
        }
    });

    panel.add(card);
}

private void showPetDetails(int petId, String name, String type, String breed, String age, String gender,
                            String vaccine, String disability, String location, String[] images) {
    JDialog dialog = new JDialog(this, "Pet Details", true);
    dialog.setSize(380, 500);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    // Background
    JPanel bg = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, new Color(38, 70, 83),
                    getWidth(), getHeight(), new Color(233, 196, 106));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    bg.setLayout(new BorderLayout());
    bg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    dialog.add(bg);

    // Main content (scrollable)
    JPanel main = new JPanel();
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.setBackground(Color.WHITE);
    main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(33, 140, 116), 2, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    JLabel title = new JLabel(name, SwingConstants.CENTER);
    title.setFont(new Font("Georgia", Font.BOLD, 24));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    title.setForeground(new Color(33, 140, 116));
    main.add(title);
    main.add(Box.createVerticalStrut(5));

    if (images.length > 0) {
        try {
            ImageIcon icon = new ImageIcon(images[0].trim());
            Image img = icon.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(img));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            main.add(imgLabel);
            main.add(Box.createVerticalStrut(10));
        } catch (Exception ignored) {}
    }

    StringBuilder info = new StringBuilder("<html><body style='text-align:center;'>"
            + "<b>Type:</b> " + type +
            "<br><b>Breed:</b> " + breed +
            "<br><b>Age:</b> " + age +
            "<br><b>Gender:</b> " + gender +
            "<br><b>Last Vaccine:</b> " + vaccine +
            "<br><b>Disability:</b> " + disability +
            "<br><b>Location:</b> " + location);

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         PreparedStatement ps = conn.prepareStatement(
                 "SELECT owners.name, owners.phone FROM owners JOIN pets ON owners.id = pets.owner_id WHERE pets.id = ?")) {
        ps.setInt(1, petId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            info.append("<br><br><b>Owner:</b> ").append(rs.getString("name"))
                    .append("<br><b>Phone:</b> ").append(rs.getString("phone"));
        }
    } catch (SQLException ex) {
        info.append("<br><i>(Owner info unavailable)</i>");
    }

    info.append("</body></html>");
    JLabel details = new JLabel(info.toString());
    details.setFont(new Font("SansSerif", Font.PLAIN, 14));
    details.setAlignmentX(Component.CENTER_ALIGNMENT);
    main.add(details);

    JPanel btns = new JPanel(new FlowLayout());
    btns.setBackground(Color.WHITE);

    JButton edit = new JButton("Edit");
    styleButton(edit, new Color(241, 196, 15));
    edit.addActionListener(e -> {
        new EditPetForm(this, petId).setVisible(true);
        dialog.dispose();
    });

    JButton request = new JButton("Request Adoption");
    styleButton(request, new Color(39, 174, 96));
    request.addActionListener(e -> {
        new AdoptionRequestForm(this, petId).setVisible(true);
        dialog.dispose();
    });

    JButton close = new JButton("Close");
    styleButton(close, new Color(231, 76, 60));
    close.addActionListener(e -> dialog.dispose());

    btns.add(edit);
    btns.add(request);
    btns.add(close);

    main.add(Box.createVerticalStrut(10));
    main.add(btns);

    // ✅ Make scrollable
    JScrollPane scrollPane = new JScrollPane(main);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    bg.add(scrollPane, BorderLayout.CENTER);

    dialog.setVisible(true);
}

private void styleButton(JButton btn, Color color) {
    btn.setBackground(color);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("SansSerif", Font.BOLD, 15));
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.setOpaque(true);
    btn.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
        public void mouseExited(MouseEvent e) { btn.setBackground(color); }
    });
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
}


}
