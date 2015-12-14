package com.the7winds.verbumSecretum.other;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by the7winds on 21.11.15.
 */
public class Connection {

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Connection(InetAddress inetAddress, int port) throws IOException {
        socket = new Socket(inetAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public Connection(final Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void send(String message) {
        try {
            Log.i("MESSAGE_SEND" + "(" + socket.getInetAddress().toString() + ")", message);
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receive(int timeout) {
        String msg = null;
        try {
            socket.setSoTimeout(timeout);
            msg = reader.readLine();
            if (msg != null) {
                Log.i("MESSAGE_RECEIVED" + "(" + socket.getInetAddress().toString() + ")", msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() throws IOException {
        socket.close();
    }

    public boolean inputReady() {
        try {
            return reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
