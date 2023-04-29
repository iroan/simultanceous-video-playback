package com.wangkaixuan.svpserver;

class App {
    public static void main(String[] args) throws Exception {
        var tmp = Config.ins().get("port");
        final int port = Integer.parseInt(tmp.toString());

        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(port);
        simpleHttpServer.start();
        // TODO: use logger
        System.out.printf("Server is started and listening on port:%d\n", port);
    }
}
