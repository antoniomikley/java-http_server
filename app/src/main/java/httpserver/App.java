package httpserver;

public class App {
    public static void main(String[] args) {
        for (int i = 0; i < 4; i++) {
            Thread worker = new Thread(new RequestHandler());
            worker.start();
        }
    }
}
