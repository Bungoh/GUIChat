package server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ServerGUI extends Application {

    private TextArea chatArea;
    private int clients = 0; // gives client ids, not an accurate representation of the number of clients online

    private ServerSocket serverSocket;
    private List<ClientInfo> clientList;

    @Override
    public void start(Stage stage) {

        //Create Root Node
        StackPane root = new StackPane();
        BorderPane borderPane = new BorderPane();

        //Create TextArea that will display Server text
        chatArea = new TextArea();
        chatArea.setEditable(false);

        //Add ChatArea to BorderPane
        borderPane.setCenter(chatArea);

        //Add BorderPane to Root
        root.getChildren().add(borderPane);

        //Set the Scene/Stage
        Scene mainScene = new Scene(root, 600, 400);
        stage.setScene(mainScene);
        stage.setTitle("Chat Server");
        stage.show();

        //Create Server
        new Thread( () -> {
            try {

                serverSocket = new ServerSocket(52864);
                chatArea.appendText("Server started at " + new Date() + "\n");
                clientList = new ArrayList<>();

                while (true) {
                    //Accept incoming connections
                    Socket socket = serverSocket.accept();

                    //Initialize the Client
                    //Get Streams
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                    //Get the name of the Client.
                    String name = inputStream.readUTF();

                    //Send client a list of people who are currently on the Server.
                    String listOfClients = "";
                    if (!clientList.isEmpty()) {
                        for (ClientInfo e : clientList) {
                            listOfClients += e.getName() + ";";
                        }
                        listOfClients = listOfClients.substring(0, listOfClients.length() - 1);
                    }

                    outputStream.writeUTF(listOfClients);
                    outputStream.flush();

                    //Create client and add it to the client list.
                    ClientInfo newClient = new ClientInfo(name, clients, socket, inputStream, outputStream);
                    clientList.add(newClient);
                    clients++;

                    //Client Connected Text
                    chatArea.appendText("Connection Created for client " + clients + " at " + new Date() + "\n");

                    //Create a new Thread for this new client.
                    new Thread(new ClientHandler(newClient)).start();
                }


            } catch (IOException ex) {
                System.out.println(ex);
            }
        }).start();

        stage.setOnCloseRequest(e -> {
            try {
                serverSocket.close();
                System.exit(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    class ClientHandler implements Runnable {

        private ClientInfo client;

        public ClientHandler(ClientInfo client) {
            // init local vars
            this.client = client;

            //Run onConnect method
            onConnect();

        }

        public void run() {
            try {
                while (true) {
                    //Read Input from Client
                    String text = client.getInStream().readUTF();

                    //Broadcast message to everyone
                    broadcast(text);
                }
            } catch (EOFException ex) {
                //Runs when a input stream is disconnected.
                disconnect();
            } catch (IOException ex) {
                System.out.println("Client: " + client.getName());
                ex.printStackTrace();
            }
        }

        /**
         * Sends a message from the Server to the specific Client.
         * @param client The client receiving the message
         * @param message The message to be sent
         * @throws IOException In the event that the message can not be sent
         */
        private void send(ClientInfo client, String message) throws IOException {
            client.getOutStream().writeUTF(message);
            client.getOutStream().flush();
        }

        /**
         * Broadcasts a message to all clients and records it in the server console as well
         * @param message The message to be sent
         * @throws IOException In the event that the message can not be sent
         */
        private void broadcast(String message) throws IOException {
            for (ClientInfo c: clientList) {
                send(c, message);
            }

            //Print to TextArea in Server Console
            chatArea.appendText(message + "\n");
        }

        /**
         * Broadcasts a message to all clients but doesn't print output to server console.
         * @param message The message to be broadcast
         * @throws IOException In the event that the message cannot be sent.
         */
        private void silentBroadcast(String message) throws IOException {
            for (ClientInfo c: clientList) {
                send(c, message);
            }
        }

        /**
         * Runs when a new client has connected to the server.
         */
        private void onConnect() {
            try {
                broadcast(client.getName() + " joined!");
                silentBroadcast("\\con:" + client.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Disconnects the client by closing the client's socket,
         * removing the client from the clientList ArrayList, and broadcasting
         * the disconnect to all other clients.
         */
        private void disconnect() {
            try {
                //Close socket and remove from ClientList
                client.getSocket().close();
                clientList.remove(client);

                //Broadcast to everyone that this client disconnected.
                broadcast(client.getName() + " has disconnected.");
                silentBroadcast("\\dis:" + client.getName());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
