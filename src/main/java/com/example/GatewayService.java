package com.example;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.example.service.GameServiceImpl;
import com.example.service.MessageServiceImpl;
import com.example.service.PlayerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import skeleton.Main;
import com.example.service.bean.client.ClientMessage;
import skeleton.service.GameStage;
import skeleton.service.Main;
import skeleton.service.MessageService;
import skeleton.service.PlayerService;

public class GatewayService {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final PlayerService playerService = new PlayerServiceImpl();
	private static final GameStage gameService = new GameServiceImpl();
	private static final MessageService messageService = new MessageServiceImpl();
	private static final Main main = new Main(playerService, gameService, messageService);

	@SuppressWarnings("unchecked")
	public static void handleClientMessage(String message, Session session) {
		ClientMessage<String> clientMessage = null;
		try {
			clientMessage = mapper.readValue(message, ClientMessage.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (clientMessage != null) {
			switch (clientMessage.getAction()) {
			case logIn:
				main.playerLogIn(session, clientMessage.getValue(String.class));
				break;
			case ready:
				playerService.getPlayerBySession(session).ifPresent(player -> {
                    main.playerReady(player);
                });
				break;
			case cellClick:
				gameService.getCellById(clientMessage.getValue(String.class)).ifPresent(cell -> {
					playerService.getPlayerBySession(session).ifPresent(player -> {
						main.playerClickedCell(player, cell);
					});
				});
				break;
			default:
				System.err.println("Unknown action");
			}
		}
	}

	/* load page */
	public static void sessionCreated(Session session) {
		messageService.sendPlayerList(session, playerService.getPlayerList());
	}

    /* disconnect */
	public static void playerDisconnect(Session session) {
		playerService.getPlayerBySession(session).ifPresent(player -> {
			main.playerLogOut(player);
		});
	}
}
