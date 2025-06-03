package com.example.remotingdemo;

import hudson.remoting.Callable;
import hudson.remoting.Channel;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MasterDemo {
    public static void main(String[] args) throws Exception {
        int port = 12345;
        // Start a server socket to simulate the master
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Master] Waiting for agent connection on port " + port + "...");
            Socket socket = serverSocket.accept();
            System.out.println("[Master] Agent connected!");

            java.util.concurrent.ExecutorService exec = java.util.concurrent.Executors.newCachedThreadPool();
            Channel channel = new Channel("master-to-agent", exec, socket.getInputStream(), socket.getOutputStream());

            java.util.Scanner scanner = new java.util.Scanner(System.in);
            boolean iterate = true;
            while (iterate) {
                System.out.print("Enter message to send to agent (or 'exit' to quit): ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    iterate = false;
                    continue;
                }
                String result = channel.call(new SimpleCallable(input));
                System.out.println("[Master] Received from agent: " + result);
                System.out.print("Do you want to request a reply from agent? (yes/no): ");
                String replyRequest = scanner.nextLine();
                if ("yes".equalsIgnoreCase(replyRequest)) {
                    System.out.print("Type your question for the agent to reply: ");
                    String question = scanner.nextLine();
                    String agentReply = channel.call(new AgentReplyCallable(question));
                    System.out.println("[Master] Agent replied: " + agentReply);
                }
                System.out.print("Continue to iterate? (yes/no): ");
                String cont = scanner.nextLine();
                if (!"yes".equalsIgnoreCase(cont)) {
                    iterate = false;
                }
            }
            channel.close();
            exec.shutdown();
        }
    }

    // Simple callable to send to the agent
    public static class SimpleCallable implements Callable<String, IOException> {
        private final String message;
        public SimpleCallable(String message) {
            this.message = message;
        }
        @Override
        public String call() throws IOException {
            System.out.println("[Agent] Received message from master: " + message);
            return "Agent received: " + message;
        }
        @Override
        public void checkRoles(org.jenkinsci.remoting.RoleChecker checker) throws SecurityException {
            // No roles to check for this demo
        }
    }

    // Callable to receive a reply from the agent
    public static class AgentReplyCallable implements Callable<String, IOException> {
        private final String message;
        public AgentReplyCallable(String message) {
            this.message = message;
        }
        @Override
        public String call() throws IOException {
            System.out.println("[Master] Received reply from agent: " + message);
            return "Master received: " + message;
        }
        @Override
        public void checkRoles(org.jenkinsci.remoting.RoleChecker checker) throws SecurityException {
            // No roles to check for this demo
        }
    }
}
