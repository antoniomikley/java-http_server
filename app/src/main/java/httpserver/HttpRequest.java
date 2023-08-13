package httpserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;

public class HttpRequest {
    HttpRequestHeader clientRequestHeader;
    byte[] clientRequestBody;
    public HttpRequest(Socket clientSocket) throws IOException {
        clientRequestHeader = new HttpRequestHeader(clientSocket);
        System.out.println(clientRequestHeader.method);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        if ("100-continue".equals(clientRequestHeader.expect)) {
            while ("100-continue".equals(clientRequestHeader.expect)) {
                System.out.println("HERE!");
                OutputStream out = clientSocket.getOutputStream();
                out.write("HTTP/1.1 100 Continue".getBytes());
                clientRequestHeader = new HttpRequestHeader(clientSocket);
                byte[] buffer = new byte[clientRequestHeader.contentLength];
                InputStream in = clientSocket.getInputStream();
                in.read(buffer);
                result.write(buffer);
            }
        } else if (clientRequestHeader.contentLength > 0) {
            byte[] buffer = new byte[clientRequestHeader.contentLength];
            InputStream in = clientSocket.getInputStream();
            in.read(buffer);
            result.write(buffer);
        }
        clientRequestBody = result.toByteArray();
    }

    public String toString() {
        return clientRequestHeader.toString() + new String(clientRequestBody) + "\r\n\r\n";
    }
    public class HttpRequestHeader {
        private String method;
        private String target;
        private String version;
        private String contentType;
        private int contentLength;
        private String expect;

        public HttpRequestHeader(Socket clientSocket) throws IOException {
            final int MAX_REQUEST_PARAM_LEN = 1024 * 8;
            InputStream incomingRequest = clientSocket.getInputStream();
            try {
                String startLine =  HttpRequest.readLineFromStream(incomingRequest, MAX_REQUEST_PARAM_LEN);
                System.out.println(startLine);
                String[] lineParts = startLine.split(" ");
                if (!"GET".equals(lineParts[0]) && !"POST".equals(lineParts[0]) && !"HEAD".equals(lineParts[0])) {
                    throw new ShittyError("Invalid Request or unsupported request method");
                }
                method = lineParts[0].strip();
                target = lineParts[1].strip();
                version = lineParts[2].strip();
            } catch (ShittyError err) {
                OutputStream out = clientSocket.getOutputStream();
                out.write("HTTP/1.1 413 Content Too Large".getBytes());
            } catch (NoSuchElementException err) {
                OutputStream out = clientSocket.getOutputStream();
                out.write("HTTP/1.1 400 Bad Request".getBytes());
            }

            String requestLine = "";
            try {
                requestLine = HttpRequest.readLineFromStream(incomingRequest, MAX_REQUEST_PARAM_LEN);
            } catch (ShittyError err) {
                OutputStream out = clientSocket.getOutputStream();
                out.write("HTTP/1.1 413 Content Too Large".getBytes());
            }
            while (!requestLine.isBlank()) {
                String[] lineParts = requestLine.split(":", 2);
                System.out.println(lineParts[0]);
                switch (lineParts[0]) {
                    case "Expect":
                        expect = lineParts[1].strip();
                        break;
                    case "Content-Type":
                        contentType = lineParts[1].strip();
                        break;
                    case "Content-Length":
                        contentLength = Integer.parseInt(lineParts[1].strip());
                        break;
                }
                try {
                    requestLine = HttpRequest.readLineFromStream(incomingRequest, MAX_REQUEST_PARAM_LEN);
                } catch (ShittyError err) {
                    OutputStream out = clientSocket.getOutputStream();
                    out.write("HTTP/1.1 413 Content Too Large".getBytes());
                }
            }
        }
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(String.format("%s %s %s\r\n", method, target, version));
            if (contentType != null) {
                result.append(String.format("Content-Type: %s\r\n", contentType));
            }
            if (contentLength != 0) {
                result.append(String.format("Content-Length: %d\r\n", contentLength));
            }
            if (expect != null) {
                result.append(String.format("Expect: %s\r\n", expect));
            }
            result.append("\r\n");
            return result.toString();
        }

        public String getMethod() {
            return method;
        }

        public String getTarget() {
            return target;
        }

        public String getVersion() {
            return version;
        }

        public String getContentType() {
            return contentType;
        }

        public int getContentLength() {
            return contentLength;
        }

        public String getExpect() {
            return expect;
        }
    }

    private static String readLineFromStream(InputStream in, int maxLineSize) throws IOException, ShittyError {
        ByteArrayOutputStream resultLine = new ByteArrayOutputStream();
        byte[] buffer = new byte[maxLineSize + 1];
        int pos = 1;
        in.read(buffer, 0, 1);
        byte carriageReturn = "\r".getBytes()[0];
        byte lineFeed = "\n".getBytes()[0];
        while (in.read(buffer, pos, 1) != -1) {
            if (buffer[pos - 1] == carriageReturn && buffer[pos] == lineFeed) {
                resultLine.write(buffer, 0, pos);
                break;
            } 
            pos++;
            if (pos == maxLineSize + 1) {
                throw new ShittyError("Number of read bytes has exceeded the maximum line length");
            }
        }
        System.out.println(resultLine);
        return resultLine.toString();
    }
}
