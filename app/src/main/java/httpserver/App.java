package httpserver;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        

        Path filePath = Paths.get(FILEROOT + "index.html");

        Iterator<String> htmlFile = Files.lines(filePath).iterator();
        System.out.println(streamToString(inputStream));
    }
}
