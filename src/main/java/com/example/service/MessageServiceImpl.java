package com.example.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.ServerMessage;
import com.example.bean.client.ClientAction;
import com.google.gson.Gson;

import reactor.rx.action.Control;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.MessageService;

public class MessageServiceImpl implements MessageService {

	private final Gson mapper;
	private final List<Player> players;

	public MessageServiceImpl() {
		mapper = new Gson();
		players = new ArrayList<>();
	}

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ServerMessage message = ServerMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(List<Cell> gameData) {
		ServerMessage message = ServerMessage.create(ClientAction.gameData, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(Cell cell) {
		ServerMessage message = ServerMessage.create(ClientAction.markedCell, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ServerMessage message = ServerMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public Control registerSession(Player player) {
		players.add(player);
		return null;
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
		broadcast(playerListMessage);
	}

	private void broadcast(ServerMessage message) {
		players.stream().forEach(player -> {
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
