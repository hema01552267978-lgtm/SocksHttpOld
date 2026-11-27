package com.slipkprojects.ultrasshservice.tunnel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class WebSocketHandshakeTest {
    @Test
    public void detectsTheSuppliedGetUpgradeStyle() {
        assertTrue(WebSocketHandshake.isUpgradeRequest(
                "GET / HTTP/1.1\\r\\nHost: example.test\\r\\nUpgrade: websocket\\r\\n\\r\\n"));
        assertFalse(WebSocketHandshake.isUpgradeRequest(
                "CONNECT example.test:22 HTTP/1.0\\r\\n\\r\\n"));
    }

    @Test
    public void consumes101HeadersAndLeavesFollowingSshBannerUnread() throws Exception {
        String response = "HTTP/1.1 101 Switching Protocols\\r\\n"
                + "Upgrade: websocket\\r\\n"
                + "Connection: Upgrade\\r\\n\\r\\n"
                + "SSH-2.0-test-server\\r\\n";
        ByteArrayInputStream input = new ByteArrayInputStream(
                response.getBytes(StandardCharsets.ISO_8859_1));

        WebSocketHandshake.consumeUpgradeResponse(input);

        assertEquals("SSH-2.0-test-server",
                readLine(input));
    }

    @Test
    public void rejectsNon101Response() throws Exception {
        assertRejected("HTTP/1.1 200 OK\\r\\nUpgrade: websocket\\r\\n\\r\\n");
    }

    @Test
    public void rejects101WithoutWebsocketUpgradeHeader() throws Exception {
        assertRejected("HTTP/1.1 101 Switching Protocols\\r\\nConnection: Upgrade\\r\\n\\r\\n");
    }

    private static void assertRejected(String response) throws Exception {
        try {
            WebSocketHandshake.consumeUpgradeResponse(new ByteArrayInputStream(
                    response.getBytes(StandardCharsets.ISO_8859_1)));
            fail("Expected an IOException");
        } catch (IOException expected) {
            // expected
        }
    }

    private static String readLine(ByteArrayInputStream input) throws Exception {
        StringBuilder result = new StringBuilder();
        int value;
        while ((value = input.read()) != -1 && value != '\\r') {
            result.append((char) value);
        }
        if (value == '\\r') {
            input.read();
        }
        return result.toString();
    }
}
