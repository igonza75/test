package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import application.SessionManager;
import java.sql.SQLException;
import java.util.List;


import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */

public class UserLoginPage {

    private final DatabaseHelper databaseHelper;
    
    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");

        loginButton.setOnAction(a -> {
            // Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();

            // Validate inputs
            if (userName.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password cannot be empty.");
                return;
            }

            try {
                User user = new User(userName, password, "", "", "");
                WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);

                // Retrieve the user's roles from the database
                List<String> roles = databaseHelper.getUserRoles(userName);

                if (roles.isEmpty()) {
                    errorLabel.setText("User account does not exist.");
                    return;
                }

                // Validate login with the first available role
                user.setRole(roles.get(0));

                if (!databaseHelper.login(user)) {
                    errorLabel.setText("Invalid username or password.");
                    return;
                }

                // Log successful login
                System.out.println("User " + userName + " logged in successfully.");

                if (roles.size() == 1) {
                    // Single role, proceed automatically
                    processLogin(primaryStage, user, roles.get(0), welcomeLoginPage);
                } else {
                    // Multiple roles, show selection dialog
                    ChoiceDialog<String> roleDialog = new ChoiceDialog<>(roles.get(0), roles);
                    roleDialog.setTitle("Select Role");
                    roleDialog.setHeaderText("Multiple roles found.");
                    roleDialog.setContentText("Please choose your role:");

                    roleDialog.showAndWait().ifPresent(selectedRole -> {
                        try {
                            user.setRole(selectedRole);
                            if (!databaseHelper.login(user)) {
                                errorLabel.setText("Invalid username or password for selected role.");
                                return;
                            }
                            processLogin(primaryStage, user, selectedRole, welcomeLoginPage);
                        } catch (SQLException e) {
                            System.err.println("Database error: " + e.getMessage());
                            errorLabel.setText("Error connecting to the database.");
                            e.printStackTrace();
                        }
                    });
                }

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                errorLabel.setText("Error connecting to the database.");
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    
    // Helper method to handle role-based navigation
    private void processLogin(Stage primaryStage, User user, String role, WelcomeLoginPage welcomeLoginPage) {
        user.setRole(role);
        SessionManager.getInstance().setCurrentUser(user.getUserName(), role);

        System.out.println("User " + user.getUserName() + " selected role: " + role);

        if (role.equals("admin")) {
            AdminHomePage adminPage = new AdminHomePage(databaseHelper);
            adminPage.show(primaryStage);
        } else {
            welcomeLoginPage.show(primaryStage, user);
        }
    }
}
