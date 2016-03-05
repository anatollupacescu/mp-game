package com.example.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.client.ClientAction;
import com.example.bean.client.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.rx.action.Control;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.MessageService;

public class MessageServiceImpl implements MessageService {

	private final ObjectMapper mapper = new ObjectMapper();
	private final List<Player> players = new ArrayList<>();

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ClientMessage<List<Player>> message = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(List<Cell> gameData) {
		ClientMessage<List<Cell>> message = ClientMessage.create(ClientAction.gameData, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(Cell cell) {
		ClientMessage<Cell> message = ClientMessage.create(ClientAction.markedCell, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ClientMessage<Player> message = ClientMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public Control registerSession(Player player) {
		players.add(player);
		return null;
	}

    @Override
	public void alert(Player player, String message) {
		sendMessage(player.getSession(), ClientMessage.createAlert(message));
	}

    @Override
	public void log(Player player, String message) {
    	sendMessage(player.getSession(), ClientMessage.createLog(message));
	}

	@Override
	public void sendPlayerList(Session session, List<Player> playerList) {
		ClientMessage<List<Player>> playerListMessage = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(playerListMessage);
	}

	private void broadcast(ClientMessage<?> message) {
		players.stream().forEach(player -> {
			sendMessage(player.getSession(), message);
		});
	}

	private void sendMessage(Session session, ClientMessage<?> clientMessage) {
		try {
			String message = mapper.writeValueAsString(clientMessage);
			if (session.isOpen()) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void alert(Session session, String message) {
		ClientMessage<String> clientMessage = ClientMessage.createAlert(message);
		sendMessage(session, clientMessage);
	}

	@Override
	public void log(Session session, String message) {
		ClientMessage<String> clientMessage = ClientMessage.createLog(message);
		sendMessage(session, clientMessage);
	}
}
