package com.example;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jetty.websocket.api.Session;

import com.example.message.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lol.Model;
import lol.service.InMemoryGame;
import lol.service.InMemoryPlayerStore;
import lol.service.RandomShuffler;

public class GatewayService {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Model main = new Model(new InMemoryPlayerStore(), new InMemoryGame(new RandomShuffler()), 64);

	public static void handleClientMessage(String message, Session session) {
		parseClientMessage(message).ifPresent(clientMessage -> {
			String data = clientMessage.getValue();
			WsSession wsSession = new WsSession(session);
			switch (clientMessage.getAction()) {
			case logIn:
				main.name(wsSession, data);
				break;
			case ready:
				main.ready(wsSession);
				break;
			case cellClick:
				main.cell(wsSession, data);
				break;
			default:
				throw new RuntimeException();
			}
		});
	}

	private static Optional<ClientMessage> parseClientMessage(String message) {
		try {
			return Optional.ofNullable(mapper.readValue(message, ClientMessage.class));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	/* load page */
	public static void sessionCreated(Session session) {
		WsSession wsSession = new WsSession(session);
		main.connect(wsSession);
	}

	/* disconnect */
	public static void playerDisconnect(Session session) {
		WsSession wsSession = new WsSession(session);
		main.disconnect(wsSession);
	}
}
