package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.Game;
import com.example.bean.GameAction;
import com.example.bean.GameMessage;
import com.example.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GameService {

	private static final Gson mapper = new GsonBuilder().create();
	private static final List<User> users = new ArrayList<User>();

	private static Game game = new Game();

	public static void markCell(String data, Session session) {
		Integer cellId = Integer.valueOf(data);
		game.markCell(cellId, session);
	}

	public static void broadcastMessage(GameMessage gameMessage) {
		String str = mapper.toJson(gameMessage);
		users.stream().forEach(user -> user.sendMessage(str));
	}

	public static void handleClientMessage(String message, Session session) {
		GameMessage gameMessage = mapper.fromJson(message, GameMessage.class);
		switch (gameMessage.getAction()) {
		case connect:
			userConnect(session);
		case logIn:
			userLogIn(session, (String) gameMessage.getData());
			Game game = GameService.getGame();
			String gameStr = mapper.toJson(game);
			GameService.broadcaseEvent(gameStr);
			break;
		// case cellClick:
		// String cellId = gameMessage.getData();
		// GameService.markCell(cellId, session);
		// GameService.broadcaseEvent(message);
		// break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static void userLogIn(Session session, String name) {
		User user = new User(session, name);
		users.add(user);
		broadcastMessage(userList(GameAction.logIn));
	}

	public static void userDisconnect(Session session) {
		Optional<User> userOptional = users.stream().filter(u -> u.session.equals(session)).findFirst();
		users.remove(userOptional.get());
		broadcastMessage(userList(GameAction.disconnect));
	}

	private static GameMessage userList(GameAction action) {
		List<String> userNameList = users.stream().map(u -> u.name).collect(Collectors.toList());
		GameMessage gameMessage = new GameMessage();
		gameMessage.setAction(action);
		gameMessage.setData(userNameList);
		return gameMessage;
	}

	public static void userConnect(Session session) {
		String str = mapper.toJson(userList(GameAction.connect));
		new User(session, null).sendMessage(str);
	}
}
