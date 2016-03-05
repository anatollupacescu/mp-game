package com.example;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jetty.websocket.api.Session;

import com.example.service.GameServiceImpl;
import com.example.service.MessageServiceImpl;
import com.example.service.PlayerServiceImpl;
import com.example.bean.client.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import skeleton.Main;
import skeleton.service.GameService;
import skeleton.service.MessageService;
import skeleton.service.PlayerService;
import skeleton.stage.MainStage;

public class GatewayService {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final PlayerService playerService = new PlayerServiceImpl();
	private static final GameService gameService = new GameServiceImpl();
	private static final MessageService messageService = new MessageServiceImpl();
	private static final Main main = new MainStage(playerService, gameService, messageService);

	public static void handleClientMessage(String message, Session session) {
		parseClientMessage(message).ifPresent(clientMessage -> {
			String data = clientMessage.getValue(String.class);
			switch (clientMessage.getAction()) {
			case logIn:
				main.playerLogIn(session, data);
				break;
			case ready:
				main.playerReady(session);
				break;
			case cellClick:
				main.playerClickedCell(session, data);
				break;
			default:
				messageService.log(session, "Unknown action");
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static Optional<ClientMessage<String>> parseClientMessage(String message) {
		try {
			return Optional.ofNullable(mapper.readValue(message, ClientMessage.class));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Optional.empty();
	}

	/* load page */
	public static void sessionCreated(Session session) {
		messageService.sendPlayerList(session, playerService.getPlayerList());
	}

	/* disconnect */
	public static void playerDisconnect(Session session) {
		main.playerLogOut(session);
	}
}
