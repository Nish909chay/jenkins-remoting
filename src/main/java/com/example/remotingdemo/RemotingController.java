package com.example.remotingdemo;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@RestController
@RequestMapping("/api/remoting")
public class RemotingController {
    private final List<String> log = Collections.synchronizedList(new ArrayList<>());
    private final SimpMessagingTemplate messagingTemplate;
    private final RemotingService remotingService;

    @Autowired
    public RemotingController(SimpMessagingTemplate messagingTemplate, RemotingService remotingService) {
        this.messagingTemplate = messagingTemplate;
        this.remotingService = remotingService;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "");
        log.add("You: " + message);
        try {
            // Only send to agent, do not echo agent's reply to frontend
            remotingService.sendMessageToAgent(message);
        } catch (Exception e) {
            String error = "[Error communicating with agent] " + e.getMessage();
            log.add("Agent: " + error);
            messagingTemplate.convertAndSend("/topic/messages", error);
        }
    }

    @PostMapping("/agent/send")
    public void agentSendMessage(@RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "");
        log.add("Agent: " + message);
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    @GetMapping("/log")
    public List<String> getLog() {
        return log;
    }
}
