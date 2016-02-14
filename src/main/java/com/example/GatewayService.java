package com.example;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.service.GameServiceImpl;
import com.example.service.MessageServiceImpl;
import com.example.service.PlayerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import skeleton.Main;
import skeleton.bean.client.ClientAction;
import skeleton.bean.client.ClientMessage;
import skeleton.bean.player.Player;
import skeleton.service.GameService;
import skeleton.service.MainService;
import skeleton.service.MessageService;
import skeleton.service.PlayerService;

public class GatewayService {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final PlayerService playerService = new PlayerServiceImpl();
	private static final GameService gameService = new GameServiceImpl();
	private static final MessageService messageService = new MessageServiceImpl();
	private static final MainService main = new Main(playerService, gameService, messageService);

	@SuppressWarnings("unchecked")
	public static void handleClientMessage(String message, Session session) {
		ClientMessage<String> clientMessage = null;
		try {
			clientMessage = mapper.readValue(message, ClientMessage.class);
		} catch (IOException e) {
			messageService.sendMessage(session, ClientMessage.createLog("Could not parse message"));
		}
		if (clientMessage != null) {
			switch (clientMessage.getAction()) {
			case logIn:
				main.playerLogIn(session, clientMessage.getValue(String.class));
				break;
			case ready:
				playerService.getPlayerBySession(session).ifPresent(player -> main.playerReady(player));
				break;
			case cellClick:
				gameService.getCellById(clientMessage.getValue(String.class)).ifPresent(cell -> {
					playerService.getPlayerBySession(session).ifPresent(player -> {
						main.playerClickedCell(player, cell);
					});
				});
				break;
			default:
				messageService.sendMessage(session, ClientMessage.createLog("Unknown action"));
			}
		}
	}

	public static void sessionCreated(Session session) {
		ClientMessage<List<Player>> message = ClientMessage.create(ClientAction.playerList,
				playerService.getPlayerList());
		messageService.sendMessage(session, message);
	}

	public static void playerDisconnect(Session session) {
		playerService.getPlayerBySession(session).ifPresent(player -> {
			main.playerLogOut(player);
		});
	}
}
