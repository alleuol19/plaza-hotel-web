package com.example.hotel;


import java.sql.*;
import java.util.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;



@Controller
public class AuthController {

    static String url = "jdbc:mysql://switchyard.proxy.rlwy.net:27217/railway";
    static String dbUser = System.getenv("root");
    static String dbPass = System.getenv("HUMUeVXnKdlhsHllvEQqxWZKErmocAhV");

    @GetMapping("/")
    public String splash() {
        return "splash";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT * FROM Users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                session.setAttribute("username", rs.getString("username"));

                if ("Admin".equalsIgnoreCase(role)) {
                    return "redirect:/admin-dashboard";
                } else {
                    return "redirect:/user-dashboard";
                }
            }

            model.addAttribute("error", "Invalid credentials!");
            return "login";

        } catch (Exception e) {
            model.addAttribute("error", "Database error: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

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

        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session, Model model) {

        int totalRooms = 0;
        int availableRooms = 0;
        int reservedRooms = 0;

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            ResultSet totalRs = conn.prepareStatement("SELECT COUNT(*) FROM Rooms").executeQuery();
            if (totalRs.next()) totalRooms = totalRs.getInt(1);

            ResultSet availableRs = conn.prepareStatement("SELECT COUNT(*) FROM Rooms WHERE isAvailable = true").executeQuery();
            if (availableRs.next()) availableRooms = availableRs.getInt(1);

            reservedRooms = totalRooms - availableRooms;

        } catch (Exception e) {
            e.printStackTrace();
        }

        int availablePercent = 0;
        int reservedPercent = 0;

        if (totalRooms > 0) {
            availablePercent = (availableRooms * 100) / totalRooms;
            reservedPercent = (reservedRooms * 100) / totalRooms;
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("reservedRooms", reservedRooms);
        model.addAttribute("availablePercent", availablePercent);
        model.addAttribute("reservedPercent", reservedPercent);

        return "admin-dashboard";
    }

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        return "user-dashboard";
    }

    @GetMapping("/book-room")
    public String bookRoomPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        return "book-room";
    }

    @PostMapping("/book-room")
    public String bookRoom(@RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam String email,
                           @RequestParam String address,
                           @RequestParam String city,
                           @RequestParam String nationality,
                           @RequestParam String passportNo,
                           @RequestParam String cardNumber,
                           @RequestParam String cvcCode,
                           @RequestParam String roomType,
                           @RequestParam String roomCapacity,
                           @RequestParam String checkInDate,
                           @RequestParam String checkOutDate,
                           @RequestParam String roomId,
                           HttpSession session) {

        String username = (String) session.getAttribute("username");

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "INSERT INTO Bookings " +
                    "(username, name, phone, email, address, city, nationality, passportNo, cardNumber, cvcCode, roomType, roomCapacity, checkInDate, checkOutDate, roomId) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, name);
            pst.setString(3, phone);
            pst.setString(4, email);
            pst.setString(5, address);
            pst.setString(6, city);
            pst.setString(7, nationality);
            pst.setString(8, passportNo);
            pst.setString(9, cardNumber);
            pst.setString(10, cvcCode);
            pst.setString(11, roomType);
            pst.setString(12, roomCapacity);
            pst.setString(13, checkInDate);
            pst.setString(14, checkOutDate);
            pst.setString(15, roomId);

            pst.executeUpdate();

            String updateRoom = "UPDATE Rooms SET isAvailable=false WHERE id=?";
            PreparedStatement updatePst = conn.prepareStatement(updateRoom);
            updatePst.setString(1, roomId);
            updatePst.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/my-booking";
    }

    @GetMapping("/my-booking")
    public String myBooking(HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        List<Map<String, String>> bookings = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT * FROM Bookings WHERE username=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("id", rs.getString("id"));
                row.put("roomType", rs.getString("roomType"));
                row.put("roomCapacity", rs.getString("roomCapacity"));
                row.put("checkIn", rs.getString("checkInDate"));
                row.put("checkOut", rs.getString("checkOutDate"));
                row.put("roomId", rs.getString("roomId"));
                bookings.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("username", username);
        model.addAttribute("bookings", bookings);

        return "my-booking";
    }

    @GetMapping("/cancel-booking")
    public String cancelBooking(@RequestParam int id) {

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String getRoom = "SELECT roomId FROM Bookings WHERE id=?";
            PreparedStatement getPst = conn.prepareStatement(getRoom);
            getPst.setInt(1, id);
            ResultSet rs = getPst.executeQuery();

            String roomId = null;
            if (rs.next()) {
                roomId = rs.getString("roomId");
            }

            String deleteSql = "DELETE FROM Bookings WHERE id=?";
            PreparedStatement deletePst = conn.prepareStatement(deleteSql);
            deletePst.setInt(1, id);
            deletePst.executeUpdate();

            if (roomId != null) {
                String updateRoom = "UPDATE Rooms SET isAvailable=true WHERE id=?";
                PreparedStatement updatePst = conn.prepareStatement(updateRoom);
                updatePst.setString(1, roomId);
                updatePst.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/my-booking";
    }

    @GetMapping("/admin-bookings")
    public String adminBookings(Model model) {

        List<Map<String, String>> bookings = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT * FROM Bookings";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("id", rs.getString("id"));
                row.put("username", rs.getString("username"));
                row.put("name", rs.getString("name"));
                row.put("roomType", rs.getString("roomType"));
                row.put("roomCapacity", rs.getString("roomCapacity"));
                row.put("checkIn", rs.getString("checkInDate"));
                row.put("checkOut", rs.getString("checkOutDate"));
                row.put("roomId", rs.getString("roomId"));
                bookings.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("bookings", bookings);
        return "admin-bookings";
    }

    @GetMapping("/admin-rooms")
    public String adminRooms(HttpSession session, Model model) {

        List<Map<String, String>> rooms = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT * FROM Rooms";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("id", rs.getString("id"));
                row.put("roomType", rs.getString("roomType"));
                row.put("roomCapacity", rs.getString("roomCapacity"));
                row.put("checkInDate", rs.getString("checkInDate"));
                row.put("checkOutDate", rs.getString("checkOutDate"));
                row.put("available", rs.getString("isAvailable"));
                rooms.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("rooms", rooms);

        return "admin-rooms";
    }

    @GetMapping("/reserved-rooms")
    public String reservedRooms(HttpSession session, Model model) {

        List<Map<String, String>> reserved = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "SELECT username, name, roomType, roomCapacity, checkInDate, checkOutDate, roomId FROM Bookings";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("name", rs.getString("name"));
                row.put("roomType", rs.getString("roomType"));
                row.put("roomCapacity", rs.getString("roomCapacity"));
                row.put("checkInDate", rs.getString("checkInDate"));
                row.put("checkOutDate", rs.getString("checkOutDate"));
                row.put("roomId", rs.getString("roomId"));
                reserved.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("reserved", reserved);

        return "reserved-rooms";
    }

    @GetMapping("/feedback")
    public String feedbackPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        return "feedback";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@RequestParam String name,
                                 @RequestParam String rating,
                                 @RequestParam String message,
                                 HttpSession session) {

        String username = (String) session.getAttribute("username");

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "INSERT INTO Feedback (username, name, rating, message) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, name);
            pst.setString(3, rating);
            pst.setString(4, message);
            pst.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/user-dashboard";
    }


@GetMapping("/admin-feedback")
public String adminFeedback(Model model) {

    List<Map<String, String>> feedbacks = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
        String sql = "SELECT * FROM Feedback";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Map<String, String> row = new HashMap<>();
            row.put("id", rs.getString("id"));
            row.put("username", rs.getString("username"));
            row.put("name", rs.getString("name"));
            row.put("rating", rs.getString("rating"));
            row.put("message", rs.getString("message"));
            feedbacks.add(row);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    model.addAttribute("feedbacks", feedbacks);
    return "admin-feedback";
}

@GetMapping("/delete-feedback")
public String deleteFeedback(@RequestParam int id) {

    try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
        String sql = "DELETE FROM Feedback WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }

    return "redirect:/admin-feedback";
}
}