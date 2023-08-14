package httpserver;

public class HttpRequestException extends Exception {
    private String httpStatusCode;
        
    public HttpRequestException(String httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public String getStatusCode() {
        return httpStatusCode;
    }
}

