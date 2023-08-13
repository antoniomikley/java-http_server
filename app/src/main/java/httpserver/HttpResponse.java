package httpserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import httpserver.HttpRequest.HttpRequestHeader;

public class HttpResponse {
    private String version;
    private String statusCode;
    private String contentType;
    private long contentLength;
    private byte[] responseBody;

    public static HttpResponse generateHttpResponse(HttpRequest clientRequest) {
        HttpResponse response = new HttpResponse();
        Path fileRoot = Paths.get("src/main/resources").toAbsolutePath();
        HttpRequestHeader requestHeader = clientRequest.clientRequestHeader;
        response.version = requestHeader.getVersion();
        if ("GET".equals(requestHeader.getMethod()) || "HEAD".equals(requestHeader.getMethod())) {
            File requestedFile = new File("");
            try {
                requestedFile = lookForTargetFile(Paths.get(fileRoot + requestHeader.getTarget()));
            } catch (IOException err) {
                response.statusCode = "404 Not Found";
                return response;
            }
            response.statusCode = "200 OK";
            response.contentType = determineContentType(requestedFile);
            response.contentLength = requestedFile.length();
            if ("HEAD".equals(requestHeader.getMethod())) {
                return response;
            }
            try {
                FileInputStream in = new FileInputStream(requestedFile);
                response.responseBody = in.readAllBytes();
                in.close();
            } catch (IOException err) {
                System.err.println("Could not read requested File.");
            }
        } 
        response.statusCode = "501 Not Implemented";
        return response;
    }

    public String responseHeadersToString() {
        StringBuilder result = new StringBuilder();
        if (version != null && statusCode != null) {
            result.append(String.format("%s %s\r\n", version, statusCode));
        } else if (contentType != null) {
            result.append(String.format("Content-Type: %s\r\n", contentType));
        } else if (contentLength > 0) {
            result.append(String.format("Content-Length: %d\r\n", contentLength));
        }
        return result.toString();
    }

    public void send(Socket clientSocket) {
        try {
            OutputStream out = clientSocket.getOutputStream();
            out.write(responseHeadersToString().getBytes());
            out.write("\r\n".getBytes());
            if (responseBody != null) {
                out.write(responseBody);
                out.write("\r\n".getBytes());
            }
            out.write("\r\n".getBytes());
        } catch (IOException err) {
            System.err.println("Could not write the Response.");
        }
    }

    public static File lookForTargetFile(Path target) throws FileNotFoundException {
        ArrayList<Path> possibleLocations = new ArrayList<>();
        possibleLocations.add(target);
        possibleLocations.add(Paths.get(target + ".html"));
        possibleLocations.add(Paths.get(target + "/index.html"));
        File targetFile = new File("");
        for (Path filePath: possibleLocations) {
            targetFile = new File(filePath.toString());
            if (targetFile.isFile()) {
                return targetFile;
            }
        }
        throw new FileNotFoundException("The requested File does not exists.");
    }

    public static String determineContentType(File targetFile) {
        String fileExtension = targetFile.getPath().substring(targetFile.getPath().lastIndexOf(".") + 1);
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
                                                                                          
