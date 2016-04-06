package com.example;

import java.io.IOException;
import java.util.List;

import com.example.message.ClientAction;
import com.example.message.ServerMessage;
import com.google.gson.Gson;

import lol.model.Cell;
import lol.model.Player;
import lol.model.Session;

public class WsSession implements Session {

	private final static Gson mapper = new Gson();
	
	private final org.eclipse.jetty.websocket.api.Session session;

	public WsSession(org.eclipse.jetty.websocket.api.Session session) {
		this.session = session;
	}

	@Override
	public void sendPlayerList(List<Player> playerList) {
		ServerMessage message = ServerMessage.create(ClientAction.playerList, playerList);
		sendMessage(message);
	}

	@Override
	public void sendGameData(List<Cell> data) {
		ServerMessage message = ServerMessage.create(ClientAction.startGame, data);
		sendMessage(message);
	}

	@Override
	public void sendErrorMessage(String string) {
		ServerMessage message = ServerMessage.createLog(string);
		sendMessage(message);
	}

	@Override
	public void sendWinner(Player winner) {
		ServerMessage message = ServerMessage.create(ClientAction.winner, winner);
		sendMessage(message);
	}

	@Override
	public void sendCheckedCell(Cell cell) {
		ServerMessage message = ServerMessage.create(ClientAction.cellClick, cell);
		sendMessage(message);
	}

	private void sendMessage(ServerMessage clientMessage) {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((session == null) ? 0 : session.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WsSession other = (WsSession) obj;
		if (session == null) {
			if (other.session != null)
				return false;
		} else if (!session.equals(other.session))
			return false;
		return true;
	}
}
