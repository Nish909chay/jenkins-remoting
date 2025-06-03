package com.example.remotingdemo;

import hudson.remoting.Channel;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RemotingService {
    private Channel channel;
    private ExecutorService exec;
    private ServerSocket serverSocket;
    private Socket socket;

    @PostConstruct
    public void startRemoting() throws IOException {
        int port = 12345;
        serverSocket = new ServerSocket(port);
        System.out.println("[RemotingService] Waiting for agent connection on port " + port + "...");
        new Thread(() -> {
            try {
                socket = serverSocket.accept();
                System.out.println("[RemotingService] Agent connected!");
                exec = Executors.newCachedThreadPool();
                channel = new Channel("master-to-agent", exec, socket.getInputStream(), socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String sendMessageToAgent(String message) throws Exception {
        if (channel == null) throw new IllegalStateException("Agent not connected");
        return channel.call(new MasterDemo.SimpleCallable(message));
    }

    public String sendAgentReplyRequest(String message) throws Exception {
        if (channel == null) throw new IllegalStateException("Agent not connected");
        return channel.call(new MasterDemo.AgentReplyCallable(message));
    }

    // Listen for agent-initiated messages in a background thread
    @PostConstruct
    public void listenForAgentMessages() {
        new Thread(() -> {
            while (true) {
                try {
                    if (channel != null && !channel.isInClosed()) {
                        // This is a placeholder for agent-initiated message polling.
                        // In a real implementation, you would have a mechanism for the agent to push messages,
                        // or you could poll for messages if the protocol supports it.
                        // For demo: sleep and continue.
                        Thread.sleep(1000);
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AgentMessageListener").start();
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (channel != null) channel.close();
        if (exec != null) exec.shutdown();
        if (socket != null) socket.close();
        if (serverSocket != null) serverSocket.close();
    }
}
