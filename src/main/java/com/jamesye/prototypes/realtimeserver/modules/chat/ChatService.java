package com.jamesye.prototypes.realtimeserver.modules.chat;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ChatService {

    private final SocketIONamespace namespace;
    private String secret;

    @Autowired
    public ChatService(SocketIOServer server, Environment env) {
        this.namespace = server.addNamespace("/chat");
        this.namespace.addConnectListener(onConnected());
        this.namespace.addDisconnectListener(onDisconnected());
        this.namespace.addEventListener("chat", ChatDTO.class, onChatReceived());
        this.namespace.addEventListener("join", String.class, onRoomJoined());
        this.namespace.addEventListener("leave", String.class, onRoomLeft());
        this.secret = env.getProperty("security.jwt.secret");
    }

    private DataListener<String> onRoomJoined() {
        return (client, roomName, ackSender) -> {
            log.info("Join room name {} ", roomName);
            client.joinRoom(roomName);
        };
    }

    private DataListener<String> onRoomLeft() {
        return (client, roomName, ackSender) -> {
            log.info("Leave room name {}", roomName);
            client.leaveRoom(roomName);
        };
    }

    private DataListener<ChatDTO> onChatReceived() {
        return (client, data, ackSender) -> {
            log.debug("Client[{}] - Received chat message '{}'", client.getSessionId().toString(), data);
            data.setMessage(data.getUserName() + ":" + data.getRoomName() + ":" + data.getMessage());
            namespace.getRoomOperations(data.getRoomName()).sendEvent("chat", data);
        };
    }

    private ConnectListener onConnected() {
        return client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            log.debug("Client[{}] - Connected to chat module through '{}'", client.getSessionId().toString(), handshakeData.getUrl());

            String token = client.getHandshakeData().getSingleUrlParam("token");
            log.info("Authenticating with token {} ... " , token);

            try {
                String json = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
            } catch (Exception e) {
                Date issuedAt = new Date();
                Date expiredAt = new Date(issuedAt.getTime() + 24*60*60*60);
                String newToken = Jwts.builder().setSubject("authen")
                        .signWith(SignatureAlgorithm.HS256, secret).setIssuedAt(issuedAt).setExpiration(expiredAt).compact();
                log.info("New token: {}", newToken);

                log.error("Authenticate fail {}", e);
                client.disconnect();
            }
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            log.debug("Client[{}] - Disconnected from chat module.", client.getSessionId().toString());
        };
    }

    private DataListener<String> onJoined() {
        return (client, data, ackSender) -> {
            log.debug("Client[{}] - Received chat message '{}'", client.getSessionId().toString(), data);
            namespace.getBroadcastOperations().sendEvent("chat", data);
        };
    }

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
}
