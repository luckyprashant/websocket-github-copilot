package com.websocket.wstutorial;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.websocket.wstutorial.dto.Message;

@Service
public class GateControlService {

	@Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private final ConcurrentHashMap<String, CompletableFuture<Message>> responseFutures = new ConcurrentHashMap<>();

	public CompletableFuture<Message> sendMessage(Message message) {
		CompletableFuture<Message> future = new CompletableFuture<>();
        responseFutures.put(message.getDeviceId(), future);

        messagingTemplate.convertAndSendToUser(message.getDeviceId(), "/topic/sita-device", message);

        return future;
	}

	public void handleResponse(Message message) {
        CompletableFuture<Message> future = responseFutures.get(message.getDeviceId());
        if (future != null) {
            future.complete(message);
        }
    }
}
