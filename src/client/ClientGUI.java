package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ClientGUI extends Application {

    private TextField chatInput;
    private Button sendButton;
    private TextArea chatArea;

    private DataOutputStream toServer = null;
    private DataInputStream fromServer = null;

    private Socket socket = null;
    private boolean running = false;

    private String name;
    private String ip;
    private int port;

    private ObservableList<String> onlineClientNames;

    public ClientGUI(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;

        start(new Stage());
    }

    @Override
    public void start(Stage stage) {

        // ----------------------- MAIN CLIENT ----------------------- //

        //Set Root Pane
        StackPane mainRoot = new StackPane();
        BorderPane borderPane = new BorderPane();

        //TextArea where Chat is Displayed
        chatArea = new TextArea();
        chatArea.setEditable(false);
        borderPane.setCenter(chatArea);

        //List View / Sidebar
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setAlignment(Pos.CENTER);

        Label sideBarLabel = new Label("Connected Users");
        sideBarLabel.setStyle("-fx-font-weight: bold");

        onlineClientNames = FXCollections.observableArrayList();
        ListView<String> listView = new ListView<>(onlineClientNames);

        sidebar.getChildren().addAll(sideBarLabel, listView);
        borderPane.setRight(sidebar);

        //Input Fields
        HBox bottomChatArea = new HBox(5);
        //TextInput
        chatInput = new TextField();
        chatInput.setAccessibleHelp("Enter message here.");
        //Button
        sendButton = new Button("Send!");

        //Send Server Information
        sendButton.setOnAction((e) -> {
            //Button has been pressed.

            //Check if toServer is null
            if (toServer == null) {
                return;
            }

            //Stop if chatInput is empty
            if (chatInput.getText().isEmpty()) {
                return;
            }

            //Attempt to send the message to the server
            try {
                String text = chatInput.getText();
                text = name + ": " + text;
                toServer.writeUTF(text);
                toServer.flush();

                chatInput.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Listen for ENTER key press
        sendButton.setDefaultButton(true);

        //Add components to HBox
        bottomChatArea.getChildren().addAll(chatInput, sendButton);
        bottomChatArea.setAlignment(Pos.CENTER);

        //Add HBox to BorderPane
        borderPane.setBottom(bottomChatArea);
        BorderPane.setMargin(bottomChatArea, new Insets(12, 12, 12, 12));

        //Add the BorderPane to Root Node
        mainRoot.getChildren().add(borderPane);
        Scene mainScene = new Scene(mainRoot, 600, 400);

        // ----------------------- CREATE STAGE ----------------------- //

        //Set the Scene/Stage
        stage.setScene(mainScene);
        stage.setTitle("Chat Application");
        stage.getIcons().add(new Image("resources/images/chat.png"));
        stage.show();

        // ----------------------- HANDLE NETWORKING ----------------------- //

        //Connect to Server
        try {
            //Create socket
            socket = new Socket(ip, port);

            //Create Streams
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());

            //Send username to the server
            toServer.writeUTF(name);
            toServer.flush();

            //Client now running
            chatArea.appendText("Connected to server.\n");
            running = true;

            //Receive list of online names
            String list = fromServer.readUTF();
            if (!list.equals("")) {
                onlineClientNames.addAll(Arrays.asList(list.split(";")));
            }

            //Thread to Receive Incoming Stream from Server
            new Thread(new IncomingHandler(stage)).start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage.setOnCloseRequest(e -> {
            if (socket != null) {
                try {
                    running = false;
                    toServer.close();
                    fromServer.close();
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

    }

    //Handles Incoming Stream from Server
    class IncomingHandler implements Runnable {

        private Stage stage;

        public IncomingHandler(Stage stage) {
            this.stage = stage;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    //Read information coming from server and print it onto the text area
                    String text = fromServer.readUTF();

                    //Check if it is a command, if it isn't print the message to the textarea
                    if (!isCommand(text)) {
                        chatArea.appendText(text + "\n");
                    }
                } catch (SocketException e) {
                    //Socket got disconnected. Stop the client from running. Open up the login window again.
                    Platform.runLater(() -> {
                        stage.close();
                        new ClientLogin().start(new Stage());
                    });
                    running = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Checks if the message from the server to this client is a command.
         * @param message The message sent from the server
         * @return True if it was a command, false if it was not a command
         */
        private boolean isCommand(String message) {
            /*
            Commands:
            \dis:[name] - client [name] has disconnected.
             */
            if (message.startsWith("\\con:")) {
                String name = message.substring(message.indexOf(":") + 1);
                onlineClientNames.add(name);
                return true;
            } else if (message.startsWith("\\dis:")) {
                String name = message.substring(message.indexOf(":") + 1);
                onlineClientNames.remove(name);
                return true;
            }

            return false;

        }
    }



}
