package httpserver;

public class HttpRequest {
    HttpRequestHeader requestHeader;
    byte[] requestBody;
    
    public HttpRequest(HttpRequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    } 
    
    public HttpRequest(HttpRequestHeader requestHeader, byte[] requestBody) {
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;
    }
    
}


    /*
    public class HttpRequest {
        HttpRequestHeader clientRequestHeader;
        byte[] clientRequestBody;
        public HttpRequest(Socket clientSocket) throws IOException {
            clientRequestHeader = new HttpRequestHeader().readRequestHeader(clientSocket);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            if ("100-continue".equals(clientRequestHeader.expect)) {
                while ("100-continue".equals(clientRequestHeader.expect)) {
                    OutputStream out = clientSocket.getOutputStream();
                    out.write("HTTP/1.1 100 Continue".getBytes());
                    clientRequestHeader = new HttpRequestHeader().readRequestHeader(clientSocket);
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

        public HttpRequestHeader readRequestHeader(Socket clientSocket) throws IOException {
            final int MAX_REQUEST_PARAM_LEN = 1024 * 8;
            HttpRequestHeader requestHeader = new HttpRequestHeader();
            InputStream incomingRequest = clientSocket.getInputStream();
            try {
                String startLine =  HttpRequest.readLineFromStream(incomingRequest, MAX_REQUEST_PARAM_LEN);
                String[] lineParts = startLine.split(" ");
                if (!"GET".equals(lineParts[0]) && !"POST".equals(lineParts[0]) && !"HEAD".equals(lineParts[0])) {
                    OutputStream out = clientSocket.getOutputStream();
                    out.write("HTTP/1.1 400 Bad Request".getBytes());
                } else {
                    requestHeader.method = lineParts[0].strip();
                    requestHeader.target = lineParts[1].strip();
                    requestHeader.version = lineParts[2].strip();
                }
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
                s#@witch (lineParts[0]) {
                    case "Expect":
                        requestHeader.expect = lineParts[1].strip();
                        break;
                    case "Content-Type":
                        requestHeader.contentType = lineParts[1].strip();
                        break;
                    case "Content-Length":
                        requestHeader.contentLength = Integer.parseInt(lineParts[1].strip());
                        break;
                }
                try {
                    requestLine = HttpRequest.readLineFromStream(incomingRequest, MAX_REQUEST_PARAM_LEN);
                } catch (ShittyError err) {
                    OutputStream out = clientSocket.getOutputStream();
                    out.write("HTTP/1.1 413 Content Too Large".getBytes());
                }
            }
            return requestHeader;
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

    public static String readLineFromStream(InputStream in, int maxLineSize) throws IOException, ShittyError {
        ByteArrayOutputStream resultLine = new ByteArrayOutputStream();

        byte[] buffer = new byte[maxLineSize + 1];
        int pos = 1;
        in.read(buffer, 0, 1);
        byte carriageReturn = "\r".getBytes()[0];
        byte lineFeed = "\n".getBytes()[0];
        while (in.read(buffer, pos, 1) != -1) {
            if (buffer[pos - 1] == carriageReturn && buffer[pos] == lineFeed) {
                resultLine.write(buffer, 0, pos + 1);
                break;
            } 
            pos++;
            if (pos == maxLineSize + 1) {
                throw new ShittyError("Number of read bytes has exceeded the maximum line length");
            }
        }
        return resultLine.toString();
    }
}*/
