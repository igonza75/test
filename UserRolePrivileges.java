package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import databasePart1.DatabaseHelper; // Ensure DatabaseHelper is imported


public class UserRolePrivileges {
    private final DatabaseHelper databaseHelper;

    public UserRolePrivileges(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Admin feature: Invite a new user
    public String generateInvitationCodeWithRoles(String role) {
        try {
            return databaseHelper.generateInvitationCode(role);
        } catch (Exception e) {
            System.err.println("Error generating invitation code: " + e.getMessage());
            return "Error: Could not generate code.";
        }
    }

    // Admin feature: Reset a user's password
    public void resetPassword(String userName) throws SQLException {
        String tempPassword = "Password2!";  // Ideally, generate a secure random password
        databaseHelper.updateUserPassword(userName, tempPassword);
        System.out.println("Temporary password set for user: " + userName);
    }

    // Admin feature: Delete a user
    public void deleteUser(String userName) {
        try {
            if (!databaseHelper.isAdmin(userName)) {
                databaseHelper.deleteUser(userName);
                System.out.println("User " + userName + " deleted.");
            } else {
                System.out.println("Cannot delete an admin user.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    // Admin feature: List all users (username, role)
    public void listAllUsers() {
        try {
            List<String> users = databaseHelper.getAllUsers();
            System.out.println("User List:");
            for (String user : users) {
                System.out.println(user);
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
    }
    
    //Lists JUST the UserNames in the dB
    public List<String> getAllUsernames() throws SQLException {
        return databaseHelper.getAllUsernames();
    }

    // Admin feature: Add a role to a user
    public void addRole(String userName, String role) {
        try {
            databaseHelper.addUserRole(userName, role);
            System.out.println("Role " + role + " added to user: " + userName);
        } catch (SQLException e) {
            System.err.println("Error adding role: " + e.getMessage());
        }
    }

    // Admin feature: Remove a role from a user
    public void removeRole(String userName, String role) {
        try {
            if (!role.equals("admin") || databaseHelper.countAdmins() > 1) {
                databaseHelper.removeUserRole(userName, role);
                System.out.println("Role " + role + " removed from user: " + userName);
            } else {
                System.out.println("Cannot remove the last admin role.");
            }
        } catch (SQLException e) {
            System.err.println("Error removing role: " + e.getMessage());
        }
    }

    // Admin feature: Get all User info from dB
    public List<User> getAllUsersAsObjects() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT userName, password, role, name, email FROM cse360users";

        try (Connection conn = databaseHelper.getConnection(); // ✅ Get valid connection
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new User(rs.getString("userName"), rs.getString("password"), rs.getString("role"),
                                   rs.getString("name"), rs.getString("email")));
            }
        }

        if (users.isEmpty()) {
            System.out.println("No users found in the database.");
        }

        return users;
    }

    // Admin feature: Update user roles in dB
    public void updateUserRoles(String userName, String roles) throws SQLException {
        databaseHelper.updateUserRoles(userName, roles);
    }

    // Placeholder for student-specific functions
    public void studentFunction() {
        System.out.println("Student-specific functionality.");
    }

    // Placeholder for instructor-specific functions
    public void instructorFunction() {
        System.out.println("Instructor-specific functionality.");
    }
} 