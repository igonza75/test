package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The User class represents a user entity in the system with properties for binding to a TableView.
 */
public class User {
    private final StringProperty userName;
    private final StringProperty password;
    private final StringProperty role;
    private final StringProperty name;  
    private final StringProperty email; 

    // Constructor to initialize a new User object with userName, password, role, name, and email.
    public User(String userName, String password, String role, String name, String email) {
        this.userName = new SimpleStringProperty(userName);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
    }

    // Getters for StringProperty (for TableView binding)
    public StringProperty userNameProperty() { return userName; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty roleProperty() { return role; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }

    // Standard Getters and Setters for direct access
    public String getUserName() { return userName.get(); }
    public void setUserName(String userName) { this.userName.set(userName); }

    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }

    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
    
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
}
