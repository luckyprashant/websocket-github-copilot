package com.websocket.wstutorial.dto;

public class Message {
    private String messageContent;

    private String deviceId;
    
    private String correlationId;
    
    
    public Message() {
		super();
	}

	public Message(String messageContent, String deviceId, String correlationId) {
		super();
		this.messageContent = messageContent;
		this.deviceId = deviceId;
		this.correlationId = correlationId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

	@Override
	public String toString() {
		return "Message [messageContent=" + messageContent + ", deviceId=" + deviceId + "]";
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
    
    
}
