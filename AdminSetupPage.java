package application;

import javafx.scene.control.TextField;
import databasePart1.DatabaseHelper;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.Scene;
import java.sql.SQLException;



public class AdminSetupPage {
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input fields for Name, Email, userName, and password
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Full Name");
        nameField.setMaxWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
      
        // Pass Confirmation, same as the one in SetupAccountPage
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm Password");
        confirmPassField.setMaxWidth(250);

        Button setupButton = new Button("Setup");

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        setupButton.setOnAction(a -> {
            // Retrieve user input
            String name = nameField.getText();
            String email = emailField.getText();
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String confirm = confirmPassField.getText();

            // Validate userName and password
            String userNameErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
          
            // Compares password and confirmation password
            String confirmPassErrMessage = "";
            if (!password.equals(confirm)) {
                confirmPassErrMessage = "Error: Passwords do not match! Please enter the same password.";
            }

            // If a returned String is not empty, it is an error message   Also adds confirmPassErrMessage
			      if (userNameErrMessage != "" || passwordErrMessage != "" || confirmPassErrMessage != "") {
				    // Display the userName and password errors in the errorLabel
            String errMessage = "";
            if (userNameErrMessage != "") // Add the userName error message
                  errMessage += "UserName Error: " + userNameErrMessage.substring(15) + "\n";
            if (passwordErrMessage != "") // Add the password error message
                  errMessage += "Password Error: " + passwordErrMessage + "\n";
            if (confirmPassErrMessage != "") // Add the confirmation error message
                  errMessage += confirmPassErrMessage;
            errorLabel.setText(errMessage); // Set the errorLabel with the complete error message
            } else {
                try {
                    // Register the admin user in the database with name and email
                    User user = new User(userName, password, "admin", name, email);
                    databaseHelper.register(user);
                    System.out.println("Administrator setup completed.");

                    // Navigate to the User Login Page
	                  // The first user must login after creating the admin account
	                  new UserLoginPage(databaseHelper).show(primaryStage);
                } catch (SQLException e) {
                    System.err.println("Database error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        VBox layout = new VBox(10, nameField, emailField, userNameField, passwordField, confirmPassField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
