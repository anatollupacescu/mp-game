package com.example.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.ServerMessage;
import com.example.bean.client.ClientAction;
import com.google.gson.Gson;

import skeleton.bean.Cell;
import skeleton.bean.Player;
import skeleton.service.MessageService;
import skeleton.service.PlayerService;

public class MessageServiceImpl implements MessageService {

	private final Gson mapper = new Gson();
	private final PlayerService playerService;

	public MessageServiceImpl(PlayerService playerService) {
		this.playerService = playerService;
	}

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ServerMessage message = ServerMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(List<Cell> gameData) {
		ServerMessage message = ServerMessage.create(ClientAction.startGame, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(int cell) {
		ServerMessage message = ServerMessage.create(ClientAction.cellClick, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ServerMessage message = ServerMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public void alert(Player player, String message) {
		sendMessage(player.getSession(), ServerMessage.createAlert(message));
	}

    @Override
	public void log(Player player, String message) {
    	sendMessage(player.getSession(), ServerMessage.createLog(message));
	}

	@Override
	public void sendPlayerList(Session session, List<Player> playerList) {
		ServerMessage playerListMessage = ServerMessage.create(ClientAction.playerList, playerList);
		sendMessage(session, playerListMessage);
	}

	private void broadcast(ServerMessage message) {
		playerService.getPlayerList().stream().forEach(player -> {
			sendMessage(player.getSession(), message);
		});
	}

	private <T> void sendMessage(Session session, ServerMessage clientMessage) {
		try {
			String message = mapper.toJson(clientMessage);
			if (session.isOpen()) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void alert(Session session, String message) {
		ServerMessage clientMessage = ServerMessage.createAlert(message);
		sendMessage(session, clientMessage);
	}

	@Override
	public void log(Session session, String message) {
		ServerMessage clientMessage = ServerMessage.createLog(message);
		sendMessage(session, clientMessage);
	}
}
