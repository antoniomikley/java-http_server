package httpserver;
import java.util.List;

public class HttpRequest {
    private HttpRequestHeader httpHeader;
    private HttpRequestBody httpBody;
    class HttpRequestHeader {
        private String Type;
        private String filePath;
        private boolean hasBody;

        HttpRequestHeader (List<String> rawRequest) {
            String[] HttpHeader = rawRequest.get(0).split(" ");
            Type = HttpHeader[0];
            filePath = HttpHeader[1];
            hasBody = rawRequest.contains("Expect: 100-continue");
            if (hasBody) {
                content_length = raw.Request
            }
        }

        private static int getContentLength(List<String> rawRequest) {
            
        }
    }
    class HttpRequestBody {
    }

    private static void parseRequest(String rawRequest) {
        
    }
}
