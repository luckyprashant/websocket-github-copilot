package com.websocket.wstutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;


public class WebSocketConnectionLostHandlerDecorator extends WebSocketHandlerDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketConnectionLostHandlerDecorator.class);

    public WebSocketConnectionLostHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
    	String deviceId = session.getPrincipal().getName();
        UserHandshakeHandler.removeDeviceFromCache(deviceId);
        LOG.info("Device '{}' disconnected and removed from cache", deviceId);
        super.afterConnectionClosed(session, closeStatus);
    }
}