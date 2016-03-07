package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.websocket.api.Session;

import skeleton.bean.player.Player;
import skeleton.service.PlayerService;

public class PlayerServiceImpl implements PlayerService {

	private final List<Player> playerList = new ArrayList<>();
	
	@Override
	public Player addPlayer(Session session, String name) {
		Player player = new Player(session, name);
		playerList.add(player);
		player.setColor(playerList.size());
		return player;
	}

	@Override
	public void removePlayer(Player player) {
		playerList.remove(player);
	}

	@Override
	public boolean allPlayersReady() {
		Optional<Player> player = playerList.stream().filter(p -> !p.isReady()).findFirst();
		return !player.isPresent();
	}

	@Override
	public Optional<Player> getPlayerBySession(Session session) {
		return playerList.stream().filter(player -> player.getSession().equals(session)).findFirst();
	}

	@Override
	public List<Player> getPlayerList() {
		return playerList;
	}

	@Override
	public boolean isPlayerLoggedIn(Session session) {
		return getPlayerBySession(session).isPresent();
	}

	enum PlayerAction {
		addPlayer, removePlayer
	}
}
