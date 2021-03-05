package client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;

public class ClientLogin extends Application {

    @Override
    public void start(Stage stage) {
        StackPane loginRoot = new StackPane();
        GridPane loginGrid = new GridPane();

        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);

        Label userLabel = new Label("Name");
        TextField userInput = new TextField();

        Label ipLabel = new Label("IP");
        TextField ipInput = new TextField();

        Label portLabel = new Label("Port");
        TextField portInput = new TextField();

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);

        Button continueButton = new Button("Submit");
        continueButton.setOnAction(e -> {

            //Check if all boxes are empty
            if (userInput.getText().isEmpty() || ipInput.getText().isEmpty() || portInput.getText().isEmpty()) {
                errorLabel.setText("ERROR: Please fill out all boxes.");
                return;
            }

            String username = userInput.getText();
            String ip = ipInput.getText();
            String port = portInput.getText();

            //Check for valid username
            if (!username.matches("\\w+")) {
                errorLabel.setText("ERROR: Invalid user name");
                return;
            }

            //Create Client GUI
            ClientGUI gui = new ClientGUI(username, ip, Integer.parseInt(port));

            //Close this GUI
            stage.close();

        });

        continueButton.setDefaultButton(true);

        VBox submitBox = new VBox();
        submitBox.setAlignment(Pos.CENTER);
        submitBox.setSpacing(10);
        submitBox.getChildren().addAll(continueButton, errorLabel);

        loginGrid.add(userLabel, 0, 0, 1, 1);
        loginGrid.add(userInput, 1, 0, 1, 1);
        loginGrid.add(ipLabel, 0, 1, 1, 1);
        loginGrid.add(ipInput, 1, 1, 1, 1);
        loginGrid.add(portLabel, 0, 2, 1, 1);
        loginGrid.add(portInput, 1, 2, 1, 1);
        loginGrid.add(submitBox, 0, 3, 2, 1);

        loginRoot.getChildren().add(loginGrid);
        
        Scene loginScene = new Scene(loginRoot, 300, 250);
        loginScene.getStylesheets().add("resources/css/clientlogin.css");

        stage.setScene(loginScene);
        stage.setTitle("Chat Application");
        stage.getIcons().add(new Image("resources/images/chat.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
