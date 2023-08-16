package httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestHandler implements Runnable {
    @Override
    public void run() {
        while (true) {
            Socket clientSocket;
            ServerSocket requestListener;
            try {
                requestListener = new ServerSocket(8080);
                clientSocket = requestListener.accept();
                requestListener.close();
            } catch (IOException err) {
                System.err.println("Could not create a socket.");
                break;
            } 
            long start = System.currentTimeMillis();
            try {
                while (System.currentTimeMillis() - start < 3000) {
                InputStream in = clientSocket.getInputStream();
                    if (in.available() > 0) {
                        System.out.println(in.available());
                        HttpRequestHeader clientRequest = HttpRequestHeader.createFromStream(in);
                        System.out.println("3");
                        OutputStream out = clientSocket.getOutputStream();
                        HttpResponse response = HttpResponse.createResponseFromHeader(clientRequest);
                        System.out.println("4");
                        response.send(out);
                        out.close();
                        System.out.println("5");
                        start = System.currentTimeMillis();
                    }
                    in.close();
                }
                clientSocket.close();
                System.out.println("7");
            } catch (IOException err) {
                System.err.println("something went wrong.");
                err.printStackTrace();
            }
        }
    }
}
