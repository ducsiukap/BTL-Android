package com.example.ddht.data.remote;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SimpleStompClient {
    private WebSocket ws;
    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, StompMessageListener> subs = new HashMap<>();
    private final String url;
    private boolean connected = false;

    public interface StompMessageListener { void onMessage(String payload); }
    public SimpleStompClient(String url) { this.url = url; }

    public void connect() {
        client.newWebSocket(new Request.Builder().url(url).build(), new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                ws = webSocket;
                sendFrame("CONNECT", "accept-version:1.2\nhost:10.0.2.2", "");
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                if (text.startsWith("CONNECTED")) {
                    connected = true;
                    subs.forEach((d, l) -> sendFrame("SUBSCRIBE", "id:sub-" + d.hashCode() + "\ndestination:" + d, ""));
                } else if (text.startsWith("MESSAGE")) {
                    int bodyIdx = text.indexOf("\n\n");
                    if (bodyIdx == -1) bodyIdx = text.indexOf("\r\n\r\n");
                    if (bodyIdx != -1) {
                        String body = text.substring(bodyIdx).replace("\u0000", "").trim();
                        for (String l : text.split("\n")) {
                            if (l.startsWith("destination:")) {
                                String d = l.substring(12).trim();
                                if (subs.containsKey(d)) new Handler(Looper.getMainLooper()).post(() -> subs.get(d).onMessage(body));
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response r) {
                connected = false;
                Log.e("STOMP", "Connect error: " + t.getMessage());
                new Handler(Looper.getMainLooper()).postDelayed(SimpleStompClient.this::connect, 5000);
            }
        });
    }

    public void subscribe(String dest, StompMessageListener listener) {
        subs.put(dest, listener);
        if (connected) sendFrame("SUBSCRIBE", "id:sub-" + dest.hashCode() + "\ndestination:" + dest, "");
    }

    private void sendFrame(String cmd, String hdr, String body) {
        if (ws != null) ws.send(cmd + "\n" + hdr + "\n\n" + body + "\u0000");
    }

    public void disconnect() {
        if (ws != null) ws.close(1000, null);
        connected = false;
    }
}
