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

        // 🧱 Top panel for buttons
JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
topPanel.setBackground(new Color(30, 39, 46)); // dark elegant background
add(topPanel, BorderLayout.NORTH);


        // Background gradient
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

        // Header
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


        // Buttons
        // Buttons
JButton donateBtn = new JButton("List a Pet for Adoption");
styleButton(donateBtn, new Color(244, 162, 97));

// Add only the remaining buttons to the top panel
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

        // Admin login (double click title)
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



        

        // Pet panel
        JPanel petPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        petPanel.setBackground(new Color(38, 70, 83));
        JScrollPane scrollPane = new JScrollPane(petPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        background.add(scrollPane, BorderLayout.CENTER);

        // Load pets from DB
        loadPetsFromDatabase(petPanel);

        // Footer
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        JLabel footerText = new JLabel("© 2025 AdoptEase | Bringing joy, one paw at a time");
        footerText.setForeground(Color.WHITE);
        footerText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.add(footerText);
        background.add(footer, BorderLayout.SOUTH);

        // Open ListPetForm when clicking “List a Pet for Adoption”
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

        // 🔹 Only show pets that are not adopted
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


    private void addPetCard(JPanel panel, int petId, String name, String type, String breed, String age, String gender,
                        String vaccine, String disability, String location, String imageURLs) {

    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    card.setPreferredSize(new Dimension(260, 260));

    String[] images = imageURLs != null ? imageURLs.split(",") : new String[]{};
    JLabel imageLabel;

    // 🐾 Load and smooth the first image
    if (images.length > 0) {
        try {
            String path = images[0].trim();
            ImageIcon icon;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                icon = new ImageIcon(new java.net.URL(path));
            } else {
                icon = new ImageIcon(path);
            }
            Image img = icon.getImage().getScaledInstance(250, 170, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel = new JLabel("Image not available", SwingConstants.CENTER);
        }
    } else {
        imageLabel = new JLabel("No image available", SwingConstants.CENTER);
    }

    card.add(imageLabel, BorderLayout.CENTER);

    // 🐾 Only show pet name
    JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    nameLabel.setOpaque(true);
    nameLabel.setBackground(Color.WHITE);
    nameLabel.setForeground(Color.DARK_GRAY);
    card.add(nameLabel, BorderLayout.SOUTH);

    // 🖱️ Click to show detailed info
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
    dialog.setSize(500, 600);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    // 🌈 Gradient background
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
    bg.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    dialog.add(bg);

    // 📦 White inner panel
    JPanel main = new JPanel();
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.setBackground(Color.WHITE);
    main.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(33, 140, 116), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));

    JLabel title = new JLabel(name, SwingConstants.CENTER);
    title.setFont(new Font("Georgia", Font.BOLD, 26));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    title.setForeground(new Color(33, 140, 116));
    main.add(title);

    // 🖼️ Show pet image
    if (images.length > 0) {
        try {
            ImageIcon icon = new ImageIcon(images[0].trim());
            Image img = icon.getImage().getScaledInstance(360, 260, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(img));
            imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imgLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            main.add(imgLabel);
        } catch (Exception ignored) {}
    }

    // 🐶 Pet details text
    StringBuilder info = new StringBuilder("<html><body style='text-align:center;'>"
            + "<b>Type:</b> " + type +
            "<br><b>Breed:</b> " + breed +
            "<br><b>Age:</b> " + age +
            "<br><b>Gender:</b> " + gender +
            "<br><b>Last Vaccine:</b> " + vaccine +
            "<br><b>Disability:</b> " + disability +
            "<br><b>Location:</b> " + location);

    // 🔹 Fetch and add owner info
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

    // 🟡 Buttons section
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

    // Show only correct buttons
    if (currentAdopterName.equalsIgnoreCase(getOwnerNameForPet(petId))) {
        btns.add(edit);
    } else {
        btns.add(request);
    }
    btns.add(close);

    main.add(Box.createVerticalStrut(15));
    main.add(btns);

    bg.add(new JScrollPane(main), BorderLayout.CENTER);
    dialog.setVisible(true);
}

private String getOwnerNameForPet(int petId) {
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
         PreparedStatement ps = conn.prepareStatement(
                 "SELECT owners.name FROM owners JOIN pets ON owners.id = pets.owner_id WHERE pets.id = ?")) {
        ps.setInt(1, petId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("name");
    } catch (SQLException ignored) {}
    return "";
}



    private void showMoreImages(String[] urls) {
        JFrame frame = new JFrame("More Photos");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        panel.setBackground(Color.WHITE);

        for (String url : urls) {
            try {
                ImageIcon icon = new ImageIcon(new java.net.URL(url.trim()));
                Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(img));
                panel.add(imgLabel);
            } catch (Exception e) {
                JLabel imgLabel = new JLabel("Image not found", SwingConstants.CENTER);
                panel.add(imgLabel);
            }
        }

        JScrollPane scroll = new JScrollPane(panel);
        frame.add(scroll);
        frame.setVisible(true);
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
