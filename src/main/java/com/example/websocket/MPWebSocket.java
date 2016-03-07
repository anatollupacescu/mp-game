package com.example.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.example.GatewayService;

@WebSocket
public class MPWebSocket {

	private Session session;
	
	@OnWebSocketMessage
	public void handleMessage(String message) {
		GatewayService.handleClientMessage(message, session);
	}

	@OnWebSocketConnect
	public void handleConnect(Session session) {
		this.session = session;
		GatewayService.sessionCreated(session);
	}

	@OnWebSocketClose
	public void handleClose(int statusCode, String reason) {
		GatewayService.playerDisconnect(session);
	}

	@OnWebSocketError
	public void handleError(Throwable error) {
		error.printStackTrace();
	}
}