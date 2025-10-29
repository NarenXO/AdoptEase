import java.sql.*;

public class PetAdoptionApp {
    static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption";
static final String DB_USER = "petapp";
static final String DB_PASS = "pet123";


    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Connected to database successfully!");
            conn.close();
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
}
