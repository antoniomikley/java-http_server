package httpserver;

import java.net.Socket;
import java.util.Timer;
import java.io.IOException;
import java.net.ServerSocket;

public class App {
    static Socket createSocket(int port) {
        Socket clientSocket = null;
        try { 
            ServerSocket serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("shit");
        }
        return clientSocket;
    }

    static void handleRequest() {
        try (Socket clientSocket = createSocket(8080)) {
            HttpResponse.generateHttpResponse(new HttpRequest(clientSocket)).send(clientSocket); 
        } catch (IOException err) {
            System.err.println("This should not happen and I really don't want to deal with it.");
        }
    }

    public static void main(String[] args) throws IOException {
        while (true) {
            handleRequest();
        }
    }
}
