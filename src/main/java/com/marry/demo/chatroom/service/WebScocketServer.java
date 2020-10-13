package com.marry.demo.chatroom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/room/{userName}")
@Component
public class WebScocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebScocketServer.class);

    private static final ConcurrentHashMap<String, Session> SESSION_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userName") String userName) {
        if (SESSION_CONCURRENT_HASH_MAP.containsKey(userName)) {
            sendText("名字" + userName + "已经被使用啦，换一个吧", session);
            return;
        }
        SESSION_CONCURRENT_HASH_MAP.put(userName, session);
        sendText("欢迎[" + userName + "]加入群聊！");

    }

    @OnMessage
    public void onMessage(String msg,@PathParam("userName")String userName){
        sendText(userName + ":\r\n" + msg);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userName") String userName) {
        sendText("好友[" + userName + "]退出群聊");
        SESSION_CONCURRENT_HASH_MAP.remove(userName);
    }

    @OnError
    public void onError(Throwable throwable) {
        logger.error("出错啦", throwable);

    }

    private void sendText(String msg) {
        sendText(msg, null);
    }

    private void sendText(String msg, Session session) {
        // 针对性发送还是全局发送
        if (session != null) {
            session.getAsyncRemote().sendText("personal" + msg);
            return;
        }

        SESSION_CONCURRENT_HASH_MAP.values().forEach((se) -> {
            se.getAsyncRemote().sendText(msg);
        });

    }
}
