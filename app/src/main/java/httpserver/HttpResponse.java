package httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class HttpResponse {
    private String httpVersion;
    private String statusCode;
    private String contentType;
    private long contentLength;
    private byte[] responseBody;

    /**
    * Creates a suitable http response by taking in a request header.
    * @param   requestHeader   the http request header on which basis a 
    *                          response is created
    * @returns                 a http response fullfilling the request
    */
    public static HttpResponse createResponseFromHeader(
            HttpRequestHeader requestHeader) {

        HttpResponse response = new HttpResponse();

        // Just setting the HTTP Version to 1.1 since we do not support other
        // versions anyway.
        response.httpVersion = "HTTP/1.1";

        // If there already was an Error while parsing the Headers then we 
        // can stop early and just tell the Client the Status Code.
        if (requestHeader.getHttpErrorCode() != null) {
            response.statusCode = requestHeader.getHttpErrorCode();
            return response;
        }
        // Theoretically this is not needed for several reasons.
        // First there should never be a value in the Expect Header except 
        // 100-continue according to the HTTP specifications.
        // Secondly the scenarios in which this header is utilized will already
        // be handled when parsing the request since they are associated with
        // e.g. the POST method which we currently do not support.
        if ("100-continue".equals(requestHeader.getExpect())) {
            response.statusCode = "100 Continue";
            return response;
        } else if (requestHeader.getExpect() != null) {
            response.statusCode = "417 Expectation Failed";
            return response;
        }
        // In case all is well, we can try reading in the requested file.
        try {
            File requestedFile = getTargetFile(requestHeader.getTarget());
            FileInputStream in = new FileInputStream(requestedFile);
            response.responseBody = in.readAllBytes();
            in.close();
            response.contentType = determineContentType(requestedFile.getPath());
            response.contentLength = response.responseBody.length;
            response.statusCode = "200 OK";
        } catch (FileNotFoundException err) {
            response.statusCode = "404 Not Found";
            return response;
        } catch (IOException err) {
            response.statusCode = "500 Internal Server Error";
            System.err.println("The requested file exist, but could not be read.");
            return response;
        }
        return response;
    }

    /**
    * Writes the HttpResponse to an OutputStream. Preferably the one that 
    * belongs to the same Socket the request was read from.
    * @param   out the OutputStream the response is written to
    * @throws  IOException if writing to the stream fails, possibly because
    *                      the client closed the socket
    */ 
    public void send(OutputStream out) throws IOException {
        System.out.println(responseHeadersToString());
        out.write(responseHeadersToString().getBytes());
            out.write("\r\n".getBytes());
        if (responseBody != null) {
            out.write(responseBody);
            out.write("\r\n".getBytes());
        }
        out.write("\r\n".getBytes());
    }
    /**
    * Produces a String representation of the HttpResponse object formated 
    * according to the http spec.
    * @returns    the http response as String
    */
    public String responseHeadersToString() {
        StringBuilder result = new StringBuilder();
        if (httpVersion != null && statusCode != null) {
            result.append(String.format("%s %s\r\n", httpVersion, statusCode));
        } else if (contentType != null) {
            result.append(String.format("Content-Type: %s\r\n", contentType));
        } else if (contentLength > 0) {
            result.append(String.format("Content-Length: %d\r\n", contentLength));
        }
        return result.toString();
    }
    
    /**
    * Yields the file that got requested if it exists.
    * It looks for that file under the given path or one level deeper for an 
    * index.html file. E.g the target could be '/test' and if there is no file
    * named 'test' or 'test.html' it looks for '/test/index.html'.
    * @param   requestTarget   a String of the path to the requested resource
    * @returns                 the file found at that path
    * @throws  FileNotFoundException   if the file could not be found
    */
    private static File getTargetFile(String requestTarget) 
            throws FileNotFoundException {
        final String FILE_ROOT = Paths
            .get("src/main/resources")
            .toAbsolutePath()
            .toString();
        String[] possibleLocations = new String[] {
            FILE_ROOT + requestTarget,
            FILE_ROOT + requestTarget + ".html",
            FILE_ROOT + requestTarget + "/index.html"
        };
        
        for (String filePath: possibleLocations) {
            File targetFile = new File(filePath);
            if (targetFile.isFile()) {
                return targetFile;
            }
        }
        throw new FileNotFoundException();
    }

    /**
    * Determines the mime type from the file name
    * @param   fileName    String of the file name with extension or a complete
    *                      or partial path to the file as long as the file
    *                      extension is part of it.
    * @returns             the appropriate mime type as String
    */
    private static String determineContentType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String mimeType;
        switch (fileExtension) {
            case "html":
                mimeType = "text/html";
                break;
            case "css":
                mimeType = "text/css";
                break;
            case "js":
                mimeType = "text/javascript";
                break;
            case "txt":
                mimeType = "text/plain";
                break;
            case "jpg":
            case "jpeg":
                mimeType = "image/jpeg";
                break;
            case "png":
                mimeType = "image/png";
                break;
            default:
                mimeType = "application/octet-stream";
        }
        return mimeType;
   }
}
