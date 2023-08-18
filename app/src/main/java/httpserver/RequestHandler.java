package httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class RequestHandler implements Runnable {
    BlockingQueue<Socket> requestQueue;

    public RequestHandler(BlockingQueue<Socket> requestQueue) {
        this.requestQueue = requestQueue;
    }
    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = requestQueue.take();
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();
                HttpRequestHeader requestHeader = new HttpRequestHeader().createFromStream(in);
                HttpResponse response = new HttpResponse().createResponseFromHeader(requestHeader);
                response.send(out);
                clientSocket.close();
            }
        } catch (InterruptedException err) {
            err.printStackTrace();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }
}
