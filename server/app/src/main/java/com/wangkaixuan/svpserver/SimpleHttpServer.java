package com.wangkaixuan.svpserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    private HttpServer httpServer;

    public SimpleHttpServer(int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            var login = new LoginHandler();
            httpServer.createContext("/login", login);

            var status = new StatusHandler();
            httpServer.createContext("/status", status);

            httpServer.setExecutor(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.httpServer.start();
    }
}