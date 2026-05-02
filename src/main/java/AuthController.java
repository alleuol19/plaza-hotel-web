import java.sql.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    // ✅ DATABASE CONFIG (FIXED)
    static String url = "jdbc:mysql://switchyard.proxy.rlwy.net:27217/railway";
    static String dbUser = "root";
    static String dbPass = "HUMUeVXnKdlhsHllvEQqxWZKErmocAhV"; // 🔥 CHANGE THIS

    // SPLASH
    @GetMapping("/")
    public String splash() {
        return "splash";
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // LOGIN PROCESS
    @PostMapping("/login")
    public String loginProcess(@RequestParam String username,
                              @RequestParam String password,
                              Model model) {

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {

            String sql = "SELECT * FROM Users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {

                String role = rs.getString("role");

                
                model.addAttribute("username", rs.getString("username"));

                if ("Admin".equalsIgnoreCase(role)) {
                    return "admin-dashboard";
                } else {
                    return "user-dashboard";
                }

            } else {
                model.addAttribute("error", "Invalid credentials!");
                return "login";
            }

        } catch (SQLException e) {
            model.addAttribute("error", "Database error!");
            return "login";
        }
    }

    // SIGNUP PAGE
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // SIGNUP PROCESS
    @PostMapping("/signup")
    public String signupProcess(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {

            String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'User')";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);

            pst.executeUpdate();

            return "redirect:/login";

        } catch (SQLException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "signup";
        }
    }

    // ADMIN DASHBOARD
    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // USER DASHBOARD
    @GetMapping("/user-dashboard")
    public String userDashboard() {
        return "user-dashboard";
    }
}