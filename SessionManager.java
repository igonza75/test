package application;

public class SessionManager {
    private static SessionManager instance;
    private String currentUserName;
    private String currentUserRole; // Add role storage

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(String userName, String role) {
        this.currentUserName = userName;
        this.currentUserRole = role;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }
}
