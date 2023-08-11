package httpserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HttpRequest {
    public HttpRequest(SocketChannel socketChannel) {

    }
    public class HttpRequestHeader {
        private String method;
        private String target;
        private String version;
        private String contentType;
        private String contentLength;
        private String expect;
        private Iterator<String> requestBody;

        public HttpRequestHeader(String rawRequestHeader) throws ShittyError {
            Iterator<String> headerLines = rawRequestHeader.lines().iterator();

            try {
                String[] startLine = headerLines.next().split(" ");
                method = startLine[0];
                target = startLine[1];
                version = startLine[2]; 
            } catch (NoSuchElementException err) {
                throw new ShittyError("Invalid Request.");
            } catch (ArrayIndexOutOfBoundsException err) {
                throw new ShittyError("Invalid Request.");
            }
            
            while (headerLines.hasNext()) {
                String line = headerLines.next();
                if (line.isBlank()) { 
                    requestBody = headerLines;
                    break;
                }
                String[] lineParts = line.split(":", 1);
                switch (lineParts[0]) {
                    case "Content-Type":
                        contentType = lineParts[1].strip();
                        break;
                    case "Content-Length":
                        contentLength = lineParts[1].strip();
                        break;
                    case "Expect":
                        expect = lineParts[1].strip();
                        break;
                }
            }
        }

        public Iterator<String> getRemainingRequestBody() {
            return requestBody;
        }
    }
    
    public class HttpRequestBody {
        public String requestBody;
        public HttpRequestBody(HttpRequestHeader requestHeader) {
            Iterator<String> remainingBody = requestHeader.getRemainingRequestBody();
            StringBuilder result = new StringBuilder();
            while (remainingBody.hasNext()) {
                result.append(remainingBody.next());
            }
            requestBody = result.toString();
        }
    }
    /**
    * Reads the HTTP request header from the Sockets Channel up to a length of
    * BUFFER_SIZE. This limit is in place to avoid clients sending a infinitely
    * long request and therefore blocking the executing thread indefinitly.
    * Instead an Error is thrown and the Client receives a response with the
    * 413 Status Code.
    * If the request header could be read successfully, it is returned as a 
    * String.
    * @param    socketChannel   the Client Sockets channel the header is read
    *                           from
    * @return                   the request header as String
    * @throws   IOException     if reading from socketChannel failes 
    * @throws   ShittyError     if the header exceeds a reasonable length
    */
    private static String readRequestHeader(SocketChannel socketChannel) throws IOException, ShittyError {
        final int BUFFER_SIZE = 1024 * 800 + 1;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int numOfBytesRead = socketChannel.read(buffer);

        if (numOfBytesRead == BUFFER_SIZE) {
            byte[] response = "HTTP/1.1 413 Content Too Large".getBytes();
            socketChannel.write(ByteBuffer.allocate(response.length).put(response));    
            throw new ShittyError("Incoming Http Client Request was too long.");
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] resultBuffer = new byte[BUFFER_SIZE - 1];
        buffer.get(resultBuffer);
        result.write(resultBuffer, 0, numOfBytesRead);

        return result.toString();
    }
}
