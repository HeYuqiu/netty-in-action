package com.hyq.netty.cp1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * socket服务端
 *
 * @author Yuqiu.He
 * @date 2020-01-11
 */
public class BlockingIOServer {
    private final ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Socket> clients = new LinkedList<>();


    public BlockingIOServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public synchronized void start() throws IOException {
        if (!serverSocket.isClosed()) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                // 每个socket起一个线程处理
                executor.execute(() -> {
                    try {
                        handleConnet(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void handleConnet(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
        String req, resp;
        while ((req = in.readLine()) != null) {
            if ("client done".equals(req)) {
                out.println("server done");
                break;
            }
            // 处理请求
            resp = processRequest(processRequest(req));
            // 返回处理结果
            out.println(resp);
            out.flush();
        }
    }

    private String processRequest(String request) {
        System.out.println(Thread.currentThread().getName() + "Server processed request:" + request);
        return "Server processed request:" + request;
    }

    public static void main(String[] args) throws IOException {
        BlockingIOServer server = new BlockingIOServer(9527);
        server.start();
    }
}
