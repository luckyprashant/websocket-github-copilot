package com.websocket.wstutorial;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.wstutorial.dto.Message;

@RestController
public class DeviceController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
    
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
    private final Map<String, String> deviceCorellationMap = new ConcurrentHashMap<>();
    
    Logger log = LoggerFactory.getLogger(DeviceController.class);


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
        
        Object correlationIdbj = headerAccessor.getNativeHeader("correlationId").get(0);

        if (correlationIdbj != null) {
        	String correlationId = headerAccessor.getNativeHeader("correlationId").get(0);
            //  Send the response to the reply queue, using the correlation ID.
        	log.info("CorrelationId found from front end: {}", correlationId);
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

        String correlationId = UUID.randomUUID().toString();
        log.info("Correlation id generated: {}", correlationId);
        message.setCorrelationId(correlationId);
        deviceCorellationMap.put(correlationId, message.getDeviceId());
        CorrelationData sdad = new CorrelationData(correlationId);
        
        org.springframework.amqp.core.Message amqpMessage = MessageConverterUtil.convertPayloadToByteArrayMessage(
                message, // Your payload object
                correlationId // Using deviceId as correlationId
        );
        
        //  Send the message to the device's queue and wait for the response.
        Object response = rabbitTemplate.convertSendAndReceive(
                DEVICE_RESPONSE_EXCHANGE,
                "", //  Use the default routing key.  The device will need to listen to the correct queue.
                amqpMessage,
                m -> {
                    MessageProperties props = m.getMessageProperties();
                    props.setCorrelationId(correlationId);
                    return m;
                },
                sdad);
        

        if (response != null) {
            return "Message sent. Device response: " + response.toString();
        } else {
            return "Error sending message or receiving response (timeout).";
        }
    }

    @RabbitListener(queues = DEVICE_RESPONSE_QUEUE)
    public void handleDeviceResponseFromQueue(org.springframework.amqp.core.Message amqpMessage) {
    	
        MessageProperties properties = amqpMessage.getMessageProperties();
        if (properties != null) {
            String correlationId = properties.getCorrelationId();
            Map<String, Object> stompHeaders = new HashMap<>();
            stompHeaders.put("correlationId", correlationId);
            try {
                // Convert byte array to Message object
                ObjectMapper objectMapper = new ObjectMapper();
                Message deviceRequest = objectMapper.readValue(amqpMessage.getBody(), Message.class);
                
                messagingTemplate.convertAndSendToUser(deviceIdFromCorrelationId(correlationId), "/topic/sita-device", deviceRequest, stompHeaders);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error converting message body to Message object: " + e.getMessage());
            }
        }
    }
    
    private String deviceIdFromCorrelationId(String correlationId) {
        return deviceCorellationMap.get(correlationId);
    }


}
