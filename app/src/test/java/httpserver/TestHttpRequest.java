package httpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

public class TestHttpRequest {
    @Test
    void readLineFromStreamReadsLinesSeparatedByCRLF() throws ShittyError, IOException {
        String testInput = "1st line\r\n2nd line\r\n";
        InputStream targetStream = new ByteArrayInputStream(testInput.getBytes());
        String firstLine = HttpRequest.readLineFromStream(targetStream, 8192);
        assertEquals("1st line\r\n", firstLine);
    }

    @Test
    void readLineFromStreamReturnsNothingIfNoCRLF() throws ShittyError, IOException {
        String testInput = "1st line\n2nd line\n";
        InputStream targetStream = new ByteArrayInputStream(testInput.getBytes());
        String firstLine = HttpRequest.readLineFromStream(targetStream, 8192);
        assertEquals("", firstLine);
    }

    @Test 
    void readLineFromStringThrowsErrorWhenLineTooLong() {
        String testInput = "1st line\r\n2nd line\r\n";
        InputStream targetStream = new ByteArrayInputStream(testInput.getBytes());
        assertThrows(ShittyError.class, () -> {HttpRequest.readLineFromStream(targetStream, 5);});
    }
}
