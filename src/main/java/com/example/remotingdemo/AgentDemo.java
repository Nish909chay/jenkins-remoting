package com.example.remotingdemo;

import hudson.remoting.Callable;
import hudson.remoting.Channel;
import java.io.IOException;
import java.net.Socket;

public class AgentDemo {
    public static void main(String[] args) throws Exception {
        // Connect to the master
        System.out.println("[Agent] Connecting to master on port 12345...");
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("[Agent] Connected to master!");

            java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newCachedThreadPool();
            Channel channel = new Channel("agent-to-master", exec, socket.getInputStream(), socket.getOutputStream());

            // Wait for callables from the master and allow agent to reply
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            Thread agentReplyThread = new Thread(() -> {
                while (!channel.isInClosed()) {
                    try {
                        if (System.in.available() > 0) {
                            String agentMsg = scanner.nextLine();
                            System.out.println("[Agent] You typed: " + agentMsg);
                            // Send reply to master using a callable
                            String reply = channel.call(new MasterDemo.AgentReplyCallable(agentMsg));
                            System.out.println("[Agent] Reply sent to master, master responded: " + reply);
                            // Send agent-initiated message to backend for frontend broadcast
                            try {
                                java.net.URL url = new java.net.URL("http://localhost:8080/api/remoting/agent/send");
                                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                                con.setRequestMethod("POST");
                                con.setRequestProperty("Content-Type", "application/json; utf-8");
                                con.setDoOutput(true);
                                String jsonInputString = "{\"message\": \"" + agentMsg.replace("\"", "\\\"") + "\"}";
                                try (java.io.OutputStream os = con.getOutputStream()) {
                                    byte[] input = jsonInputString.getBytes("utf-8");
                                    os.write(input, 0, input.length);
                                }
                                int code = con.getResponseCode();
                                System.out.println("[Agent] Sent message to frontend, response code: " + code);
                            } catch (Exception e) {
                                System.out.println("[Agent] Failed to send message to frontend: " + e.getMessage());
                            }
                        }
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
            agentReplyThread.setDaemon(true);
            agentReplyThread.start();
            channel.join();
            channel.close();
            exec.shutdown();
        }
    }
}
