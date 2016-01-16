package com.example;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class MpGameServiceWebSocket {

    private Session session;

    @OnWebSocketMessage
    public void handleMessage(String message) {
        GameService.handleClientMessage(message, session);
    }

    @OnWebSocketConnect
    public void handleConnect(Session session) {
        this.session = session;
        GameService.sessionCreated(session);
    }

    @OnWebSocketClose
    public void handleClose(int statusCode, String reason) {
        GameService.playerDisconnect(session);
    }

    @OnWebSocketError
    public void handleError(Throwable error) {
        error.printStackTrace();
    }
}
