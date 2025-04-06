package com.websocket.wstutorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import java.io.IOException;

public class MessageConverterUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Message convertPayloadToByteArrayMessage(Object payload, String correlationId) throws IOException {
        byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);

        MessageProperties props = new MessageProperties();
        props.setCorrelationId(correlationId);
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setContentEncoding("UTF-8");

        return MessageBuilder
                .withBody(payloadBytes)
                .andProperties(props)
                .build();
    }

    public static <T> T convertByteArrayMessageToPayload(Message message, Class<T> targetClass) throws IOException {
        return objectMapper.readValue(message.getBody(), targetClass);
    }

    
}
