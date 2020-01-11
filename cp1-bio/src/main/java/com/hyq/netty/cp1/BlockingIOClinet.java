package com.hyq.netty.cp1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Yuqiu.He
 * @date 2020-01-11
 */
public class BlockingIOClinet {
    private final int serverPort;
    private final Socket socket;
    private PrintWriter out;

    public BlockingIOClinet(int serverPort) throws IOException {
        socket = new Socket();
        this.serverPort = serverPort;
    }

    public void start() throws IOException {
        if (!socket.isConnected()) {
            socket.connect(new InetSocketAddress(serverPort));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 异步接受消息
            new Thread(() -> {
                try {
                    this.receiveMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public synchronized void sendMsg(String msg) throws IOException {
        out.println(msg);
    }

    private void receiveMsg() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        String req;
        while (true) {
            if ((req = in.readLine()) != null) {
                if ("server done".equals(req)) {
                    break;
                }
                // 收到服务端返回结果
                System.out.println("[client]:response -> " + req);
            }
        }
    }


    public static void main(String[] args) throws IOException {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    BlockingIOClinet socket = new BlockingIOClinet(9527);
                    socket.start();
                    socket.sendMsg(Thread.currentThread().getName() + "hyq");
                    socket.sendMsg(Thread.currentThread().getName() + "zhushu");
                    socket.sendMsg(Thread.currentThread().getName() + "client done");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}
