package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientInfo {

    private String name;
    private int id;
    private Socket socket;
    private DataInputStream inStream;
    private DataOutputStream outStream;

    public ClientInfo(String name, int id, Socket socket, DataInputStream inStream, DataOutputStream outStream) {
        this.name = name;
        this.id = id;
        this.socket = socket;
        this.inStream = inStream;
        this.outStream = outStream;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInStream() {
        return inStream;
    }

    public DataOutputStream getOutStream() {
        return outStream;
    }
}
