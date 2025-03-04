package com.DVMS.websocket;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class VisitorWebSocketHandler extends TextWebSocketHandler {

    private ConcurrentHashMap<Long, WebSocketSession> visitorSessions = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(VisitorWebSocketHandler.class);
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        String visitorIdString = uri.substring(uri.lastIndexOf('/') + 1);
        Long visitorId = Long.parseLong(visitorIdString);
        visitorSessions.put(visitorId, session);
        logger.info("WebSocket connection established for visitorId: " + visitorId);
        logger.info("WebSocket Session saved. visitorId: " + visitorId + ", Session: " + session);
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        visitorSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        System.out.println("WebSocket connection closed.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle client messages if needed
    }

    public void sendMessageToVisitor(Long visitorId, String message) throws IOException {
        System.out.println("Attempting to send message to visitorId: " + visitorId + ", message: " + message);
        WebSocketSession session = visitorSessions.get(visitorId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                System.out.println("Sending message to visitorId: " + visitorId + ", message: " + message);
            } catch (IOException e) {
                System.err.println("Error sending message to visitorId: " + visitorId + ": " + e.getMessage());
            }
        } else {
        	System.out.println("WebSocket session state for visitorId " + visitorId + ": " + (session != null ? session.isOpen() : "null"));
        }
    }

    @Scheduled(fixedRate = 30000) // Send a ping every 30 seconds
    public void sendPing() throws IOException {
        for (WebSocketSession session : visitorSessions.values()) {
            if (session.isOpen()) {
                session.sendMessage(new PingMessage());
            }
        }
    }
}