package httpserver;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testing HttpRequestHeader")
public class TestHttpRequestHeader {
    @Test
    @DisplayName("A valid GET Request creates an appropriate HttpRequestHeader")
    void testGetRequestCreatesValidHttpRequestHeader() throws IOException {
        byte[] getRequest = "GET / HTTP/1.1\r\n".getBytes();
        InputStream in = new ByteArrayInputStream(getRequest);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertAll("http request header",
            () -> assertEquals("GET", requestHeader.getRequestMethod()),
            () -> assertEquals("/", requestHeader.getTarget()),
            () -> assertEquals("HTTP/1.1", requestHeader.getHttpVersion()),
            () -> assertNull(requestHeader.getExpect()),
            () -> assertNull(requestHeader.getContentType()),
            () -> assertEquals(0, requestHeader.getContentLength()),
            () -> assertNull(requestHeader.getHttpErrorCode())
        );
    }

    @Test
    @DisplayName("Too long Headers cause a 431 Request Header Fields Too Large Status Code")
    void testTooLongHeadersCauseErrorCode() throws IOException {
        StringBuilder header = new StringBuilder("Cookie: ");
        for (int i = 0; i <= 8192; i++) {
            header.append("x");
        }
        header.append("\r\n");
        byte[] request = ("GET / HTTP/1.1\r\n" + header.toString()).getBytes();
        InputStream in = new ByteArrayInputStream(request);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertEquals("431 Request Header Fields Too Large", requestHeader.getHttpErrorCode());
    }

    @Test
    @DisplayName("Too many request headers causes 431 Request Header Fields Too Large")
    void testTooManyHeadersCauseErrorCode() throws IOException {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i <= 100; i++) {
            header.append("Cookie: xxx\r\n");
        }
        header.append("\r\n");
        byte[] request = ("GET / HTTP/1.1\r\n" + header.toString()).getBytes();
        InputStream in = new ByteArrayInputStream(request);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertEquals("431 Request Header Fields Too Large", requestHeader.getHttpErrorCode());
    }

    @Test
    @DisplayName("Request with unsupported HTTP Version causes 505 HTTP Version Not Supported")
    void testUnsupportedHttpVersionCausesErrorCode() throws IOException {
        byte[] request = "GET / HTTP/1.0\r\n".getBytes();
        InputStream in = new ByteArrayInputStream(request);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertEquals("505 HTTP Version Not Supported", requestHeader.getHttpErrorCode());
    }

    @Test
    @DisplayName("Unsupported request method causes 501 Not Implemented")
    void testUnsupportedRequestMethodCausesErrorCode() throws IOException {
        byte[] request = "PUT / HTTP/1.1\r\n".getBytes();
        InputStream in = new ByteArrayInputStream(request);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertEquals("501 Not Implemented", requestHeader.getHttpErrorCode());
    }

    @Test
    @DisplayName("Invalid Request Syntax causes 400 Bad Request")
    void testInvalidRequestSyntaxCausesErrorCode() throws IOException {
        byte[] request = "PUT / HTTP/1.1\n".getBytes();
        InputStream in = new ByteArrayInputStream(request);
        HttpRequestHeader requestHeader = HttpRequestHeader.createFromStream(in);
        assertEquals("400 Bad Request", requestHeader.getHttpErrorCode());
    }

}
