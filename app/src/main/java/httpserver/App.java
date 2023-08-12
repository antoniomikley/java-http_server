package httpserver;

import java.net.Socket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

public class App {
    public static String FILEROOT = "/home/antonio/uni/http-server/app/src/main/resources/";
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

    public static String streamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = inputStream.read(buffer)) > 0) {
            result.write(buffer, 0 ,count);
            if (result.toString().contains("\r\n\r\n")) {
                break;
            }

        }
        return result.toString();
    }




    public static void main(String[] args) throws IOException {
        
        Socket clientSocket = createSocket(8080);
        HttpRequest test = new HttpRequest(clientSocket);
        System.out.println(test.toString());
    }
}
