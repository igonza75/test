package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
    private final DatabaseHelper databaseHelper;

    // Constructor to initialize with DatabaseHelper
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
        // Input fields for userName, password, invitation code, name, and email
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Full Name");
        nameField.setMaxWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        // Pass Confirmation
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm Password");
        confirmPassField.setMaxWidth(250);

        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        setupButton.setOnAction(a -> {
            // Retrieve user input
            String name = nameField.getText();
            String email = emailField.getText();
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String confirm = confirmPassField.getText();
            String code = inviteCodeField.getText();

            // Validate userName and password
            String userNameErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
            String confirmPassErrMessage = "";
            if (!password.equals(confirm)) { // Compares password and confirmation password
                confirmPassErrMessage = "Error: Passwords do not match! Please enter the same password."; 
            } // Gives error if they do not match

            // If a returned String is not empty, it is an error message. Also included pass confirmation
            if (!userNameErrMessage.isEmpty() || !passwordErrMessage.isEmpty() || !confirmPassErrMessage.isEmpty()) {
                // Display the userName, password, and confirmation errors in the errorLabel
                String errMessage = "";
                if (!userNameErrMessage.isEmpty()) // Add the userName error message
                    errMessage += "UserName Error: " + userNameErrMessage.substring(15) + "\n";
                if (!passwordErrMessage.isEmpty()) // Add the password error message
                    errMessage += "Password Error: " + passwordErrMessage + "\n";
                if (!confirmPassErrMessage.isEmpty()) // Add the confirmation error message
                    errMessage += confirmPassErrMessage;
                errorLabel.setText(errMessage);
                return;
            }

            try {
                if (!databaseHelper.doesUserExist(userName)) {
                    // Get roles from the invitation code
                    String rolesString = databaseHelper.getRolesFromInvitationCode(code);
                    
                    if (rolesString != null) {
                        List<String> roles = Arrays.asList(rolesString.split(","));

                        // Check if there are multiple roles
                        if (roles.size() > 1) {
                            // Prompt user to select a role
                            ChoiceDialog<String> roleDialog = new ChoiceDialog<>(roles.get(0), roles);
                            roleDialog.setTitle("Select Role");
                            roleDialog.setHeaderText("Multiple roles found.");
                            roleDialog.setContentText("Please choose your role:");

                            roleDialog.showAndWait().ifPresent(selectedRole -> {
                                try {
                                    // Create user with the selected role
                                    User user = new User(userName, password, selectedRole, name, email);
                                    databaseHelper.register(user);

                                    // Navigate to the Welcome Login Page
                                    new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                                } catch (SQLException e) {
                                    System.err.println("Database error: " + e.getMessage());
                                    errorLabel.setText("An error occurred while setting up the account. Please try again.");
                                    e.printStackTrace();
                                }
                            }); 

                        } else {
                            // If only one role, proceed with that role
                            User user = new User(userName, password, roles.get(0), name, email);
                            databaseHelper.register(user);

                            // Navigate to the Welcome Login Page
                            new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                        }

                    } else {
                        errorLabel.setText("Please enter a valid or unused invitation code.");
                    }
                } else {
                    errorLabel.setText("This username is taken! Please use another.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                errorLabel.setText("An error occurred while setting up the account. Please try again.");
                e.printStackTrace();
            }
        }); 

        VBox layout = new VBox(10, nameField, emailField, userNameField, passwordField, confirmPassField, inviteCodeField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    } 
}
 
