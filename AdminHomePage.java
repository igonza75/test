package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;
import java.sql.SQLException;


public class AdminHomePage {
	private final DatabaseHelper databaseHelper;
	private final UserRolePrivileges userRolePrivileges;

	public AdminHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
		this.userRolePrivileges = new UserRolePrivileges(databaseHelper);
	}

	/** 
	 * Displays the admin page in the provided primary stage.
	 * 
	 * @param primaryStage The primary stage where the scene will be displayed.
	 */
	public void show(Stage primaryStage) {
		VBox layout = new VBox(20);
		layout.setPadding(new Insets(20));
		layout.setAlignment(Pos.CENTER);

		// Title
		Label title = new Label("Admin Dashboard");
		title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

		// Buttons for admin actions
		Button inviteUserButton = createButton("Invite User");
		Button listUsersButton = createButton("List All Users");
		Button logoutButton = createButton("Logout");

		// Event handlers for buttons
		inviteUserButton.setOnAction(e -> inviteUser());
		listUsersButton.setOnAction(e -> listAllUsers());
		logoutButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));

		layout.getChildren().addAll(title, inviteUserButton, listUsersButton, logoutButton);

		Scene scene = new Scene(layout, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Admin Dashboard");
		primaryStage.show();
	}

	// Helper method to create consistent styled buttons
	private Button createButton(String text) {
		Button button = new Button(text);
		button.setStyle(
				"-fx-font-size: 16px; -fx-background-color: #2a9df4; -fx-text-fill: white; -fx-padding: 10 20;");
		button.setMaxWidth(300);
		return button;
	}

	// Admin actions implementation
	private void inviteUser() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Role Selection");
		dialog.setHeaderText("Select roles for the new user:");

		CheckBox adminCheckBox = new CheckBox("Admin");
		CheckBox studentCheckBox = new CheckBox("Student");
		CheckBox reviewerCheckBox = new CheckBox("Reviewer");
		CheckBox instructorCheckBox = new CheckBox("Instructor");
		CheckBox staffCheckBox = new CheckBox("Staff");

		VBox checkboxLayout = new VBox(10, adminCheckBox, studentCheckBox, reviewerCheckBox, instructorCheckBox,
				staffCheckBox);
		checkboxLayout.setPadding(new Insets(20));

		dialog.getDialogPane().setContent(checkboxLayout);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		dialog.showAndWait().ifPresent(buttonType -> {
			if (buttonType == ButtonType.OK) {
				StringBuilder selectedRoles = new StringBuilder();
				if (adminCheckBox.isSelected())
					selectedRoles.append("admin,");
				if (studentCheckBox.isSelected())
					selectedRoles.append("student,");
				if (reviewerCheckBox.isSelected())
					selectedRoles.append("reviewer,");
				if (instructorCheckBox.isSelected())
					selectedRoles.append("instructor,");
				if (staffCheckBox.isSelected())
					selectedRoles.append("staff,");

				if (selectedRoles.length() > 0)
					selectedRoles.setLength(selectedRoles.length() - 1);

				String invitationCode = userRolePrivileges.generateInvitationCodeWithRoles(selectedRoles.toString());
				showAlert("Invitation Code",
						"Generated Invitation Code: " + invitationCode + "\nRoles: " + selectedRoles);
			}
		});
	}

	private void resetPasswordForSelectedUser(TableView<User> table) {
    User selectedUser = table.getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
        showAlert("No Selection", "Please select a user to reset their password.");
        return;
    }

    try {
        // Reset the user's password
        userRolePrivileges.resetPassword(selectedUser.getUserName());
        showAlert("Password Reset", "A temporary password has been set for user: " + selectedUser.getUserName());

        // Refresh the table data
        table.getItems().clear(); // Clear the current items
        table.getItems().addAll(userRolePrivileges.getAllUsersAsObjects()); // Reload updated user data
    } catch (SQLException e) {
        showAlert("Error", "Failed to reset the password for user: " + selectedUser.getUserName());
        e.printStackTrace();

    }
}

	private void listAllUsers() {
	    Stage userListStage = new Stage();
	    userListStage.setTitle("All Users");

	    TableView<User> table = new TableView<>();

	    // Define table columns
	    TableColumn<User, String> nameCol = new TableColumn<>("Name");
	    nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
	    nameCol.setPrefWidth(200);

	    TableColumn<User, String> emailCol = new TableColumn<>("Email");
	    emailCol.setCellValueFactory(data -> data.getValue().emailProperty());
	    emailCol.setPrefWidth(250);

	    TableColumn<User, String> userNameCol = new TableColumn<>("Username");
	    userNameCol.setCellValueFactory(data -> data.getValue().userNameProperty());
	    userNameCol.setPrefWidth(200);

	    TableColumn<User, String> roleCol = new TableColumn<>("Role");
	    roleCol.setCellValueFactory(data -> data.getValue().roleProperty());
	    roleCol.setPrefWidth(300);

	    TableColumn<User, String> passwordCol = new TableColumn<>("Password");
	    passwordCol.setCellValueFactory(data -> data.getValue().passwordProperty());
	    passwordCol.setPrefWidth(200);

	    // Add columns to the table
	    table.getColumns().addAll(nameCol, emailCol, userNameCol, roleCol, passwordCol);
	    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

	    // Fetch and populate users
	    try {
	        List<User> users = userRolePrivileges.getAllUsersAsObjects();
	        if (users.isEmpty()) {
	            System.out.println("No users available to display.");
	        } else {
	            System.out.println("Displaying " + users.size() + " users.");
	        }
	        table.getItems().setAll(users);
	    } catch (SQLException e) {
	        System.err.println("Error loading users: " + e.getMessage());
	    }

	    // Toolbar with "Manage User Roles" and "Reset Password" buttons
	    ToolBar toolBar = new ToolBar();
	    Button manageRolesButton = new Button("Manage User Roles");
	    manageRolesButton.setOnAction(e -> manageUserRolesForSelectedUser(table));

	    Button resetPasswordButton = new Button("Reset Password");
	    resetPasswordButton.setOnAction(e -> resetPasswordForSelectedUser(table));
	    Button deleteUserButton = new Button("Delete User");
	    deleteUserButton.setOnAction(e -> deleteUserWithConfirmation(table));

	    toolBar.getItems().addAll(manageRolesButton, resetPasswordButton, deleteUserButton);

	    // Layout for the window
	    VBox layout = new VBox(toolBar, table);
	    layout.setSpacing(10);
	    layout.setPadding(new Insets(20));

	    Scene scene = new Scene(layout, 900, 600);
	    userListStage.setScene(scene);
	    userListStage.show();
	}
	
	// Helper method to delete a user with confirmation
	private void deleteUserWithConfirmation(TableView<User> table) {
	    User selectedUser = table.getSelectionModel().getSelectedItem();
	    if (selectedUser == null) {
	        showAlert("No Selection", "Please select a user to delete.");
	        return;
	    }

	    String currentUserName = getCurrentUserName(); // Fetch the current logged-in user

	    if (selectedUser.getUserName().equals(currentUserName)) {
	        showAlert("Error", "You cannot delete your own account.");
	        return;
	    }

	    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmationAlert.setTitle("Confirm Deletion");
	    confirmationAlert.setHeaderText(null);
	    confirmationAlert.setContentText("Are you sure you want to delete user: " + selectedUser.getUserName() + "?");

	    confirmationAlert.showAndWait().ifPresent(response -> {
	        if (response == ButtonType.OK) {
	            {
	                // Delete user
	                userRolePrivileges.deleteUser(selectedUser.getUserName());

	                // Refresh the table data
	                table.getItems().remove(selectedUser);
	                showAlert("Success", "User " + selectedUser.getUserName() + " deleted successfully.");
	            } 
	        }
	    });
	}

	private void manageUserRolesForSelectedUser(TableView<User> table) {
	    User selectedUser = table.getSelectionModel().getSelectedItem();
	    if (selectedUser == null) {
	        showAlert("No Selection", "Please select a user to manage roles.");
	        return;
	    }

	    String userName = selectedUser.getUserName();
	    String currentRoles = selectedUser.getRole();  // Get current roles

	    // Create a dialog for role management
	    Dialog<ButtonType> dialog = new Dialog<>();
	    dialog.setTitle("Manage Roles for " + userName);
	    dialog.setHeaderText("Select roles for the user:");

	    // Create checkboxes for each role
	    CheckBox adminCheckBox = new CheckBox("Admin");
	    CheckBox studentCheckBox = new CheckBox("Student");
	    CheckBox reviewerCheckBox = new CheckBox("Reviewer");
	    CheckBox instructorCheckBox = new CheckBox("Instructor");
	    CheckBox staffCheckBox = new CheckBox("Staff");

	    // Tick checkboxes for the current roles
	    if (currentRoles.contains("admin")) adminCheckBox.setSelected(true);
	    if (currentRoles.contains("student")) studentCheckBox.setSelected(true);
	    if (currentRoles.contains("reviewer")) reviewerCheckBox.setSelected(true);
	    if (currentRoles.contains("instructor")) instructorCheckBox.setSelected(true);
	    if (currentRoles.contains("staff")) staffCheckBox.setSelected(true);

	    VBox checkboxLayout = new VBox(10, adminCheckBox, studentCheckBox, reviewerCheckBox, instructorCheckBox, staffCheckBox);
	    checkboxLayout.setPadding(new Insets(20));

	    dialog.getDialogPane().setContent(checkboxLayout);
	    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

	    dialog.showAndWait().ifPresent(buttonType -> {
	        if (buttonType == ButtonType.OK) {
	            // Collect selected roles
	            StringBuilder selectedRoles = new StringBuilder();
	            if (adminCheckBox.isSelected()) selectedRoles.append("admin,");
	            if (studentCheckBox.isSelected()) selectedRoles.append("student,");
	            if (reviewerCheckBox.isSelected()) selectedRoles.append("reviewer,");
	            if (instructorCheckBox.isSelected()) selectedRoles.append("instructor,");
	            if (staffCheckBox.isSelected()) selectedRoles.append("staff,");

	            if (selectedRoles.length() > 0) selectedRoles.setLength(selectedRoles.length() - 1);  // Remove the last comma

	            try {
	                // Update the roles in the database
	                userRolePrivileges.updateUserRoles(userName, selectedRoles.toString());

	                // Refresh the TableView by reloading data
	                table.getItems().clear();  // Clear existing items
	                table.getItems().addAll(userRolePrivileges.getAllUsersAsObjects());  // Reload updated user data

	                showAlert("Success", "Roles updated successfully for " + userName + ".");
	            } catch (SQLException e) {
	                showAlert("Error", "Failed to update roles for " + userName + ".");
	                e.printStackTrace();
	            }
	        }
	    });
	}
	
	private String getCurrentUserName() {
	    return SessionManager.getInstance().getCurrentUserName(); // Replace with your session management logic
	}

	// Helper method to show alerts
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}