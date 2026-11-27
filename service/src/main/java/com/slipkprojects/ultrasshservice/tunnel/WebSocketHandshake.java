package com.slipkprojects.ultrasshservice.tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Reads the HTTP response to an SSH-over-WebSocket upgrade without buffering
 * bytes belonging to the SSH banner that follows the HTTP headers.
 */
final class WebSocketHandshake {
    private static final int MAX_LINE_BYTES = 8192;
    private static final int MAX_HEADER_BYTES = 32768;

    private WebSocketHandshake() {
    }

    static boolean isUpgradeRequest(String payload) {
        if (payload == null) {
            return false;
        }
        String normalized = payload.toLowerCase(Locale.US);
        return normalized.startsWith("get ") && normalized.contains("upgrade: websocket");
    }

    static void consumeUpgradeResponse(InputStream input) throws IOException {
        int[] budget = new int[] {0};
        String statusLine = readCrlfLine(input, budget);
        String[] statusParts = statusLine.split("\\s+", 3);
        if (statusParts.length < 2 || !statusParts[0].startsWith("HTTP/")
                || !"101".equals(statusParts[1])) {
            throw new IOException("WebSocket upgrade rejected: " + statusLine);
        }

        boolean websocketUpgrade = false;
        while (true) {
            String header = readCrlfLine(input, budget);
            if (header.length() == 0) {
                break;
            }
            int separator = header.indexOf(':');
            if (separator <= 0) {
                throw new IOException("Malformed WebSocket response header");
            }
            String name = header.substring(0, separator).trim();
            String value = header.substring(separator + 1).trim();
            if ("Upgrade".equalsIgnoreCase(name)
                    && "websocket".equalsIgnoreCase(value)) {
                websocketUpgrade = true;
            }
        }

        if (!websocketUpgrade) {
            throw new IOException("WebSocket upgrade response is missing Upgrade: websocket");
        }
    }

    private static String readCrlfLine(InputStream input, int[] budget) throws IOException {
        StringBuilder line = new StringBuilder();
        while (true) {
            int value = input.read();
            if (value == -1) {
                throw new IOException("Connection closed during WebSocket upgrade response");
            }
            budget[0]++;
            if (budget[0] > MAX_HEADER_BYTES) {
                throw new IOException("WebSocket response headers are too large");
            }
            if (value == '\r') {
                int next = input.read();
                if (next == -1) {
                    throw new IOException("Incomplete WebSocket response line");
                }
                budget[0]++;
                if (budget[0] > MAX_HEADER_BYTES || next != '\n') {
                    throw new IOException("Malformed WebSocket response line ending");
                }
                return line.toString();
            }
            if (value == '\n') {
                throw new IOException("Malformed WebSocket response line ending");
            }
            if (line.length() >= MAX_LINE_BYTES) {
                throw new IOException("WebSocket response line is too long");
            }
            line.append((char) (value & 0xff));
        }
    }
}
