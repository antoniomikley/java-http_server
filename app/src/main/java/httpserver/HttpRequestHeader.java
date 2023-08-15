package httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class HttpRequestHeader {
    private static final int MAX_HEADER_SIZE = 1024 * 8;
    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "HEAD");
    private static final Set<String> SUPPORTED_VERSIONS = Set.of("HTTP/1.1");
    private String requestMethod;
    private String target;
    private String httpVersion;
    private String contentType;
    private long contentLength;
    private String expect;
    private String httpErrorCode;

    /**
    * Reads an HTTP request from a InputStream and constructs a matching 
    * Instance of an HttpRequestHeader Object containing the HTTP request 
    * headers.
    * @param   in  InputStream from which the HTTP request headers are read
    * @returns     HttpRequestHeader representing the HTTP request headers
    * @throws  IOException if reading from the InputStream fails, e.g because
    *                      the connection was closed
    */
    public static HttpRequestHeader createFromStream(InputStream in) 
            throws IOException {

        HttpRequestHeader requestHeader = new HttpRequestHeader();

        try {
            requestHeader.parseStartLine(readFromStreamUntilCRLF(in));
            for (String requestLine; !(requestLine = readFromStreamUntilCRLF(in)).isBlank();) {
                String[] lineParts = requestLine.split(": ", 2);
                if (lineParts.length != 2) {
                    requestHeader.httpErrorCode = "400 Bad Request";
                    return requestHeader;
                }
                switch (lineParts[0]) {
                    case "Content-Type":
                        requestHeader.contentType = lineParts[1].strip();
                        break;
                    case "Content-Length":
                        requestHeader.contentLength = Long.parseLong(lineParts[1].strip());
                        break;
                    case "Expect":
                        requestHeader.expect = lineParts[1].strip();
                        break;
                }
            }
        } catch (HttpRequestException err) {
            requestHeader.httpErrorCode = err.getStatusCode();
            return requestHeader;
        }
        return requestHeader;
    }
    
    // Getters.. wow
    public String getRequestMethod() {
        return requestMethod;
    }

    public String getTarget() {
        return target;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getContentType() {
        return contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getExpect() {
        return expect;
    }

    public String getHttpErrorCode() {
        return httpErrorCode;
    }

    /**
    * Reads from an InputStream until a CRLF (\r\n) is hit und returns a 
    * String with what was read.
    * 
    * @param    in  the InputStream from which is beeing read  
    * @return       the String containing the bytes in the Stream until 
    *               the first CRLF
    * @throws   HttpRequestException    if the set limit for the maximum line 
    *                                   length is reached or all bytes in the 
    *                                   Stream have been read without ever 
    *                                   reaching a CRLF
    */
    private static String readFromStreamUntilCRLF(InputStream in) 
            throws IOException, HttpRequestException {

        final byte[] CRLF = "\r\n".getBytes();
        byte[] buffer = new byte[MAX_HEADER_SIZE + 1];
        int pos = 1;

        in.read(buffer, 0, 1);
        while (in.read(buffer, pos, 1) != -1) {
            if (buffer[pos - 1] == CRLF[0] && buffer[pos] == CRLF[1]) {
                return new String(buffer, 0, pos + 1);
            }
            pos++;
            if (pos == MAX_HEADER_SIZE + 1) {
                throw new HttpRequestException(
                        "431 Request Header Fields Too Large",
                        "Could not read the line from Stream since its length exceeds the set limit.");
            }
        }
        throw new HttpRequestException(
                "400 Bad Request",
                "Stream contents did not contain a CRLF.");
    }

    /**
    * Sets the Http Request Method, Request Target and Http Version for the 
    * HttpRequestHeader by inspecting the given String.
    * @param    startLine   String containing http method, target and version
    * @thros    HttpRequestException    if startLine does not contain the first
    *                                   line of a http request with valid syntax
    */
    private void parseStartLine(String startLine) throws HttpRequestException {
        String[] lineParts = startLine.split(" ");
        
        if (lineParts.length != 3) {
            throw new HttpRequestException(
                    "400 Bad Request",
                    "The request can not be processed since it does not follow valid syntax.");
        }
        if (!(SUPPORTED_METHODS.contains(lineParts[0]))) {
            throw new HttpRequestException(
                    "501 Not Implemented",
                    "The requests method is invalid or not supported.");
        }
        if (!(SUPPORTED_VERSIONS.contains(lineParts[2]))) {
            throw new HttpRequestException(
                    "503 HTTP Version Not Supported",
                    "The requests http vesion is not supported by the server.");
        }

        this.requestMethod = lineParts[0];
        this.target = lineParts[1];
        this.httpVersion = lineParts[2];
    }
}
