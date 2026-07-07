package com.example.gameclient;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {

    public interface Listener {
        void onMessage(String message);
        void onConnected();
        void onDisconnected();
    }

    private Socket socket;
    private PrintWriter out;
    private volatile Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ServerConnection(Listener listener) {
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(String ip, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                mainHandler.post(() -> { if (listener != null) listener.onConnected(); });

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    mainHandler.post(() -> { if (listener != null) listener.onMessage(msg); });
                }
            } catch (Exception e)
                mainHandler.post(() -> { if (listener != null) listener.onDisconnected(); });
            }
        }).start();
    }
    public void send(String message) {
        new Thread(() -> {
            if (out != null) out.println(message);
        }).start();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }
}