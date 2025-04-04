package com.websocket.wstutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.websocket.wstutorial.dto.Message;
import com.websocket.wstutorial.dto.ResponseMessage;

@Controller
public class GateSitaWebSocketController {

    @Autowired
    private GateControlService service;


    @MessageMapping("/action-response")
    public ResponseMessage sendPrivateMessage(Message message) {
    	service.handleResponse(message);
        return new ResponseMessage("Server received your private message: " + HtmlUtils.htmlEscape(message.getMessageContent()));
        
    }
    
}
