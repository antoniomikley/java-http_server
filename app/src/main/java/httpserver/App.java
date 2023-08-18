package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class App {

    public static void main(String[] args) throws IOException {
        BlockingQueue<Socket> requestQueue = new LinkedBlockingQueue<Socket>();
        for (int i = 0; i < 4; i++) {
            Thread worker = new Thread(new RequestHandler(requestQueue));
            worker.start();
        }
        try {
            ServerSocket requestListener = new ServerSocket(8080);
            while (true) {
                requestQueue.put(requestListener.accept());
            }
        } catch (IOException err) {
            System.err.println("Dont know why this happens.");
            err.printStackTrace();
        } catch (InterruptedException err) {
            err.printStackTrace();
        }
    }
}
