package com.example;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.example.bean.GameCommand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebSocket
public class StockServiceWebSocket {

	private static final Gson mapper = new GsonBuilder().create();

	private Session session;

	// called when the socket connection with the browser is established
	@OnWebSocketConnect
	public void handleConnect(Session session) {
		this.session = session;
	}

	// called when the connection closed
	@OnWebSocketClose
	public void handleClose(int statusCode, String reason) {
		System.out.println("Connection closed with statusCode=" + statusCode + ", reason=" + reason);
	}

	// called when a message received from the browser
	@OnWebSocketMessage
	public void handleMessage(String message) {
		GameCommand gameCommand = mapper.fromJson(message, GameCommand.class);
		switch (gameCommand.getAction()) {
		case startGame:
		case cellClick:
		default:
			send(message);
		}
	}

	// called in case of an error
	@OnWebSocketError
	public void handleError(Throwable error) {
		error.printStackTrace();
	}

	// sends message to browser
	private void send(String message) {
		try {
			if (session.isOpen()) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// closes the socket
	private void stop() {
		try {
			session.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}