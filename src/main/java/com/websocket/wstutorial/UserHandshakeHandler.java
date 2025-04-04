package com.websocket.wstutorial;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.sun.security.auth.UserPrincipal;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserHandshakeHandler.class);
    
    private static final Map<String, String> deviceCache = new ConcurrentHashMap<>();


    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String deviceId = request.getURI().getQuery().split("=")[1];
        LOG.info("Device with ID '{}' opened connected!!", deviceId);
        deviceCache.put(deviceId, deviceId);

        return new UserPrincipal(deviceId);
    }
    
    public static void removeDeviceFromCache(String deviceId) {
        deviceCache.remove(deviceId);
    }

}
