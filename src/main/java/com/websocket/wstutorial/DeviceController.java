package com.websocket.wstutorial;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.websocket.wstutorial.dto.Message;

@RestController
public class DeviceController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
    
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
    private final Map<String, String> deviceCorellationMap = new ConcurrentHashMap<>();


    private static final String DEVICE_RESPONSE_EXCHANGE = "device.response.exchange";
    private static final String DEVICE_RESPONSE_QUEUE = "device.response.queue";
    
//  Handle messages sent by devices to the server.
    @MessageMapping("/device-message")
    public void handleDeviceMessage(org.springframework.messaging.Message<Message> message) {
    	Message payload = message.getPayload();
        //  Extract the deviceId from the session.
    	SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(message);
        String deviceId = headerAccessor.getUser().getName(); // Get deviceId from the Principal
        System.out.println("Received message from device " + deviceId + ": " + payload.getMessageContent());
        
        String correlationId = headerAccessor.getHeader("correlationId").toString();

        if (correlationId != null) {
            //  Send the response to the reply queue, using the correlation ID.
            rabbitTemplate.convertAndSend(DEVICE_RESPONSE_EXCHANGE, "", payload, m -> {
                MessageProperties props = m.getMessageProperties();
                props.setCorrelationId(correlationId);
                return m;
            });
        }
    }
    
    
    //  Handle REST requests to send messages to devices.
    @PostMapping("/send-message")
    public String sendMessageToDevice(@RequestBody Message message) throws IOException {

//        String correlationId = message.getDeviceId();
        String correlationId = UUID.randomUUID().toString();
        System.out.println("hello");
        System.out.println(correlationId);
        message.setCorrelationId(correlationId);
        deviceCorellationMap.put(correlationId, message.getDeviceId());
        //  Send the message to the device's queue and wait for the response.
        Object response = rabbitTemplate.convertSendAndReceive(
                DEVICE_RESPONSE_EXCHANGE,
                "", //  Use the default routing key.  The device will need to listen to the correct queue.
                message,
                m -> {
                    MessageProperties props = m.getMessageProperties();
                    props.setCorrelationId(correlationId);
                    return m;
                });

        if (response != null) {
            return "Message sent. Device response: " + response.toString();
        } else {
            return "Error sending message or receiving response (timeout).";
        }
    }

    //  Listen for responses from devices on the dedicated RabbitMQ queue.
    @RabbitListener(queues = DEVICE_RESPONSE_QUEUE)
    public void handleDeviceResponseFromQueue(org.springframework.messaging.Message<Message> message) {
    	
    	MessageProperties properties = message.getHeaders().get("amqp_properties", MessageProperties.class);
    	if (properties != null) {
    		String correlationId = properties.getCorrelationId();
            System.out.println("Correlation ID received from RabbitMQ: " +  correlationId); // Add logging

    		SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setLeaveMutable(true);
            headerAccessor.setHeader("correlationId", correlationId);
            MessageHeaders headers = headerAccessor.getMessageHeaders();
            
    		Message deviceResponse = message.getPayload();
            messagingTemplate.convertAndSendToUser(deviceIdFromCorrelationId(correlationId), "/topic/sita-device", deviceResponse, headers);
    	}
    }
    
    private String deviceIdFromCorrelationId(String correlationId) {
    	System.out.println("Retrieving device ID for correlation ID: " + correlationId);
        String deviceId = deviceCorellationMap.get(correlationId);
        System.out.println("Device ID found: " + deviceId);
        return deviceId;  // Replace this with actual logic to retrieve device ID
    }


}
