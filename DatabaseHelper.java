package databasePart1;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


import application.User;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	
	public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connectToDatabase();
        }
        return connection;
    }

	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "name VARCHAR(255), "       // New column for Name
	            + "email VARCHAR(255), "      // New column for Email
	            + "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(255))";
	    statement.execute(userTable);
	

		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE, "
	            + "role VARCHAR(255))";
	    statement.execute(invitationCodesTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (userName, password, role, name, email) VALUES (?, ?, ?, ?, ?)";

	    try (Connection conn = getConnection();  // Ensure a fresh connection
	         PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
	         
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, user.getRole());
	        pstmt.setString(4, user.getName());
	        pstmt.setString(5, user.getEmail());

	        int rowsInserted = pstmt.executeUpdate();
	        if (rowsInserted > 0) {
	            System.out.println("User successfully registered: " + user.getUserName());
	        } else {
	            System.out.println("Failed to register user: " + user.getUserName());
	        }
	    }
	}



	public boolean login(User user) throws SQLException {
	    String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role LIKE ?";
	    
	    try (Connection conn = getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	         
	        pstmt.setString(1, user.getUserName());
	        pstmt.setString(2, user.getPassword());
	        pstmt.setString(3, "%" + user.getRole() + "%"); // Allow partial match for role

	        try (ResultSet rs = pstmt.executeQuery()) {
	            return rs.next(); // Returns true if a matching user exists
	        }
	    }
	}


	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";

	    try (Connection conn = getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	         
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getInt(1) > 0; // Return true if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if an error occurs / user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public List<String> getUserRoles(String userName) throws SQLException {
	    List<String> roles = new ArrayList<>();
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    
	    try (Connection conn = getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	         
	        pstmt.setString(1, userName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String roleString = rs.getString("role"); // Example: "admin,manager"
	                roles = Arrays.asList(roleString.split(",")); // Convert to List
	            }
	        }
	    }
	    return roles;
	}


	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String role) {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code, role) VALUES (?, ?)";

	    try (Connection conn = getConnection(); // Get a fresh connection
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, code);
	        pstmt.setString(2, role);
	        pstmt.executeUpdate();
	        return code; // Return generated code after successful insertion

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // Return null if an error occurs
	}


	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code, String name, String email, String userName, String password) {
	    String query = "SELECT role FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            String role = rs.getString("role");
	            
	            // Register the user with name and email included
	            register(new User(userName, password, role, name, email));

	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Retrieves roles associated with a valid invitation code and marks the code as used.
	public String getRolesFromInvitationCode(String code) {
	    String query = "SELECT role FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    
	    try (Connection conn = getConnection(); 
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	         
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            String roles = rs.getString("role");
	            markInvitationCodeAsUsed(code); // Mark the code as used
	            return roles;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;  // Return null if the code is invalid or already used
	}

	public void updateUserPassword(String userName, String newPassword) throws SQLException {
	    String query = "UPDATE cse360users SET password = ? WHERE userName = ?";
	    try (Connection conn = getConnection();  // Ensure we get a valid connection
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	        System.out.println("Password updated for user: " + userName);
	    }
	}


    // Deletes a user from the database
	public void deleteUser(String userName) throws SQLException {
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (Connection conn = getConnection(); // Create a new connection for this operation
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    }
	}


    // Checks if a user has an admin role
    public boolean isAdmin(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ? AND role = 'admin'";
        try (Connection conn = getConnection(); // Create a new connection for this operation
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Check if a matching record exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Returns a list of all users with their roles
    public List<String> getAllUsers() throws SQLException {

        List<String> users = new ArrayList<>();
        String query = "SELECT userName, role FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String userInfo = "Username: " + rs.getString("userName") + ", Role: " + rs.getString("role");
                users.add(userInfo);
            }
        }
        return users;
    }

    public List<String[]> getAllUserDetails() throws SQLException {
        List<String[]> userDetails = new ArrayList<>();
        String query = "SELECT userName, password, role, name, email FROM cse360users"; // ✅ Fetching all 5 columns

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String[] userData = new String[5]; // ✅ Now allocating space for 5 values
                userData[0] = rs.getString("userName");
                userData[1] = rs.getString("password");
                userData[2] = rs.getString("role");
                userData[3] = rs.getString("name");   // ✅ Fetch name
                userData[4] = rs.getString("email");  // ✅ Fetch email
                userDetails.add(userData);
            }
        }
        return userDetails;
    }


    public void updateUserRoles(String userName, String roles) throws SQLException {
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (Connection conn = getConnection(); // Ensure a valid connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roles);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
            System.out.println("Roles updated for user: " + userName);
        }
    }


 // Returns a list of all usernames (for reset password and similar operations)
    public List<String> getAllUsernames() throws SQLException {
        List<String> usernames = new ArrayList<>();
        String query = "SELECT userName FROM cse360users";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                usernames.add(rs.getString("userName"));
            }
        }
        return usernames;
    }
    
    // Adds a new role to an existing user
    public void addUserRole(String userName, String newRole) throws SQLException {
        String query = "UPDATE cse360users SET role = CONCAT(role, ',', ?) WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
            System.out.println("Role " + newRole + " added to user: " + userName);
        }
    }

    // Removes a role from an existing user
    public void removeUserRole(String userName, String roleToRemove) throws SQLException {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String roles = rs.getString("role");
                String updatedRoles = roles.replace(roleToRemove, "").replaceAll(",{2,}", ",").replaceAll("^,|,$", "");
                String updateQuery = "UPDATE cse360users SET role = ? WHERE userName = ?";
                try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
                    updatePstmt.setString(1, updatedRoles);
                    updatePstmt.setString(2, userName);
                    updatePstmt.executeUpdate();
                    System.out.println("Role " + roleToRemove + " removed from user: " + userName);
                }
            }
        }
    }

    // Counts the number of users with the admin role
    public int countAdmins() throws SQLException {
        String query = "SELECT COUNT(*) AS adminCount FROM cse360users WHERE role LIKE '%admin%'";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("adminCount");
            }
        }
        return 0;
    }

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

}