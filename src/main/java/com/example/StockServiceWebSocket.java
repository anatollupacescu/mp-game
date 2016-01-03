package com.example;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.example.service.GameService;

@WebSocket
public class StockServiceWebSocket {

	private Session session;

	@OnWebSocketMessage
	public void handleMessage(String message) {
		GameService.handleClientMessage(message, session);
	}

	@OnWebSocketConnect
	public void handleConnect(Session session) {
		this.session = session;
		GameService.userConnect(session);
	}

	@OnWebSocketClose
	public void handleClose(int statusCode, String reason) {
		GameService.userDisconnect(session);
	}

	@OnWebSocketError
	public void handleError(Throwable error) {
		error.printStackTrace();
	}
}
