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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePlayer(Player Player) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isTheLastPlayerReady(Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<Player> getPlayerBySession(Session session) {
		return playerList.stream().filter(player -> player.getSession().equals(session)).findFirst();
	}

	@Override
	public List<Player> getPlayerList() {
		// TODO Auto-generated method stub
		return null;
	}

}
