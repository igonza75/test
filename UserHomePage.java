package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	private final DatabaseHelper databaseHelper; // Store database instance, same as AdminHomePage
	
	public UserHomePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
    public void show(Stage primaryStage, DatabaseHelper databaseHelper) {
    	VBox layout = new VBox(15);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Logout button, same as AdminHomePage
	    Button logoutButton = new Button("Logout");
	    logoutButton.setMinWidth(100);
	    logoutButton.setOnAction(e -> {
	    	SetupLoginSelectionPage setupLoginPage = new SetupLoginSelectionPage(databaseHelper);
	    	setupLoginPage.show(primaryStage); // Sends to SetupLoginSelectionPage
	    });

	    layout.getChildren().addAll(userLabel, logoutButton); // Now includes logoutButton alongside userLabel
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
	    primaryStage.show(); // Make sure stage is updated
    	
    }
}