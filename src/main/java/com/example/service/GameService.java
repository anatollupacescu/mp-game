package com.example.service;

import com.example.bean.Game;
import com.example.bean.GameAction;
import com.example.bean.GameMessage;
import com.example.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameService {

    private static final Gson mapper = new GsonBuilder().create();
    private static final List<User> users = new ArrayList<>();

    private static Game game;

    public static void handleClientMessage(String message, Session session) {
        GameMessage gameMessage = mapper.fromJson(message, GameMessage.class);
        switch (gameMessage.getAction()) {
            case connect:
                userConnect(session);
                break;
            case logIn:
                userLogIn(session, gameMessage.getData());
                break;
            case ready:
                userReady(session, gameMessage.getData());
                break;
            case startGame:
                startGame(session, gameMessage);
                break;
            case cellClick:
                cellClick(session, gameMessage);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void userReady(Session session, Object data) {
        if (Boolean.TRUE.equals(data)) {
            game = new Game(users);
            broadcastMessage(new GameMessage(GameAction.startGame, gameAsJsonArray(game)));
        } else {
            sendMessage(session, new GameMessage(GameAction.ready, null));
            lookupUser(session).get().isReady(true);
            List<User> userNotReadyYet = users.stream().filter(u -> !u.ready).collect(Collectors.toList());
            if (userNotReadyYet.size() == 1L) {
                sendMessage(userNotReadyYet.iterator().next(), new GameMessage(GameAction.grantStart, null));
            }
        }
    }

    private static String gameAsJsonArray(Game game) {
        List<Integer> jsonArrayList = game.tableAsArray().stream().map(u -> (u == null) ? 0 : u.color).collect(Collectors.toList());
        return mapper.toJson(jsonArrayList);
    }

    private static void sendMessage(Session session, GameMessage msg) {
        String str = mapper.toJson(msg);
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(str);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private static void sendMessage(User u, GameMessage msg) {
        sendMessage(u.session, msg);
    }

    public static void broadcastMessage(GameMessage gameMessage) {
        users.stream().forEach(user -> sendMessage(user, gameMessage));
    }

    private static void cellClick(Session session, GameMessage gameMessage) {
        if(game.isOver()) {
            sendMessage(session, new GameMessage(GameAction.gameOver, null));
        } else if(game.markCell(lookupUser(session), (Double) gameMessage.getData())) {
            broadcastMessage(gameMessage);
            game.getWinner().ifPresent(winner -> broadcastMessage(new GameMessage(GameAction.winner, winner.name)));
        } else {
            sendMessage(session, new GameMessage(GameAction.wrongColor, null));
        }
    }

    private static void startGame(Session session, GameMessage gameMessage) {
        sendMessage(lookupUser(session).get(), gameMessage);
    }

    private static Optional<User> lookupUser(Session session) {
        return users.stream().filter(u -> u.session.equals(session)).findFirst();
    }

    public static void userLogIn(Session session, Object name) {
        User user = new User(session, 1 + users.size(), (String) name);
        users.add(user);
        broadcastMessage(userList(GameAction.logIn));
    }

    public static void userDisconnect(Session session) {
        Optional<User> userOptional = users.stream().filter(u -> u.session.equals(session)).findFirst();
        users.remove(userOptional.get());
        broadcastMessage(userList(GameAction.disconnect));
    }

    private static GameMessage userList(GameAction action) {
        return new GameMessage(action, userNames());
    }

    private static List<String> userNames() {
        return users.stream().map(u -> u.name).collect(Collectors.toList());
    }

    public static void userConnect(Session session) {
        sendMessage(session, userList(GameAction.connect));
    }
}