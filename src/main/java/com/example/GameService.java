package com.example;

import com.example.bean.GameAction;
import com.example.bean.GameMessage;
import com.example.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static reactor.bus.selector.Selectors.$;

public class GameService {

    private static final EventBus bus = EventBus.create();
    private static final AtomicInteger userCount = new AtomicInteger(0);
    private static final Gson mapper = new GsonBuilder().create();
    private static final List<User> players = new ArrayList<>();

    static {
        bus.receive($("userList"), (Event<GameMessage> ev) -> {
                GameMessage message = ev.getData();
                Object response = null;
                switch (message.getAction()) {
                    case connect:
                        response = players;
                        break;
                    case logIn:
                        User userToAdd = (User) message.getData();
                        players.add(userToAdd);
                        response = players;
                        break;
                    case disconnect:
                        User userToRemove = (User) message.getData();
                        players.remove(userToRemove);
                        response = players;
                        break;
                    case ready:
                        Session session = message.getData(Session.class);
                        players.stream().filter(u->u.session.equals(session)).findFirst().ifPresent(u -> u.isReady(true));
                        response = players.stream().filter(u -> !u.ready).collect(Collectors.toList());
                    default:
                        break;
                }
                return response;
        });
    }

    public static void handleClientMessage(String message, Session session) {
        GameMessage gameMessage = mapper.fromJson(message, GameMessage.class);
        switch (gameMessage.getAction()) {
            case connect:
                userConnect(gameMessage);
                break;
            case logIn:
                userLogIn(session, gameMessage);
                break;
            case ready:
                userReady(session, gameMessage);
                break;
            case startGame:
                startGame(session, gameMessage);
                break;
            case cellClick:
//                cellClick(session, gameMessage);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static GameMessage busUserList(GameAction action, List<User> users) {
        Object data = users.stream().map(u -> u.name).collect(Collectors.toList());
        return new GameMessage(action, data);
    }

    private static void broadcastUserList(List<User> userList) {
        GameMessage message = busUserList(GameAction.logIn, userList);
        userList.stream().forEach(u -> sendMessage(u.session, message));
    }

    private static void userLogIn(Session session, GameMessage gameMessage) {
        User user = new User(session, userCount.incrementAndGet(), gameMessage.getData(String.class));
        GameMessage userGameMessage = new GameMessage(gameMessage.getAction(), user);
        bus.sendAndReceive("userList", Event.wrap(userGameMessage), response -> {
            List<User> userList = (List<User>) response.getData();
            broadcastUserList(userList);
        });
    }

    private static void userConnect(GameMessage gameMessage) {
        bus.sendAndReceive("userList", Event.wrap(gameMessage), response -> {
            List<User> userList = (List<User>) response.getData();
            broadcastUserList(userList);
        });
    }

    public static void userConnect(Session session) {
        bus.sendAndReceive("userList", Event.wrap(new GameMessage(GameAction.connect, null)), response -> {
            GameMessage message = busUserList(GameAction.connect, (List<User>) response.getData());
            sendMessage(session, message);
        });
    }

    public static void userDisconnect(Session session) {
        bus.sendAndReceive("userList", Event.wrap(new GameMessage(GameAction.connect, null)), response -> {
            List<User> userList = (List<User>) response.getData();
            userList.stream().filter(u -> u.session.equals(session)).findFirst().ifPresent(user -> {
                GameMessage gm = new GameMessage(GameAction.disconnect, user);
                bus.sendAndReceive("userList", Event.wrap(gm), rsp -> {
                    broadcastUserList((List<User>) rsp.getData());
                });
            });
        });
    }

    private static void userReady(Session session, Object data) {
        Boolean isReady = ((GameMessage)data).getData(Boolean.class);
        if (Boolean.TRUE.equals(isReady)) {
            bus.sendAndReceive("userList", Event.wrap(new GameMessage(GameAction.connect, null)), response -> {
                List<User> userList = (List<User>) response.getData();
                bus.on($("game"), new GameConsumer(bus, userList));
                sendMessage(session, new GameMessage(GameAction.ready, null));
            });
        } else {
            sendMessage(session, new GameMessage(GameAction.ready, null));
            bus.sendAndReceive("userList", Event.wrap(new GameMessage(GameAction.ready, session)), usersNotReady -> {
                List<User> usersNotReadyYet = (List<User>) usersNotReady.getData();
                if (usersNotReadyYet.size() == 1) {
                    sendMessage(usersNotReadyYet.iterator().next().session, new GameMessage(GameAction.grantStart, null));
                }
            });
        }
    }

    private static void startGame(Session session, GameMessage gameMessage) {
        bus.sendAndReceive("userList", Event.wrap(new GameMessage(GameAction.connect, null)), response -> {
            List<User> usersReady = ((List<User>) response.getData()).stream().filter(u -> u.ready).collect(Collectors.toList());
            bus.receive($("game"), (Event<GameMessage> ev) -> {
                final GameMessage data = ev.getData();
                switch(data.getAction()) {
                    case cellClick:
                        break;

                }
                return null;
            });

        sendMessage(lookupUser(session).get(), gameMessage);
    }

    private static void sendMessage(Session session, GameMessage gameMessage) {
        String str = mapper.toJson(gameMessage);
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(str);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    /*
    private static void checkPlayerWins() {
        game.getWinner().ifPresent(winner -> {
            broadcastMessage(new GameMessage(GameAction.winner, winner.name));
            game = null;
            users = new ArrayList<>();
        });
    }

    public static void broadcastMessage(GameMessage gameMessage) {
        bus.notify($("broadcast"), Event.wrap(gameMessage));
    }

    private static void cellClick(Session session, GameMessage gameMessage) {
        if (game == null) {
            sendMessage(session, new GameMessage(GameAction.gameOver, null));
        } else if (game.markCell(lookupUser(session), (Double) gameMessage.getData())) {
            broadcastMessage(gameMessage);
            checkPlayerWins();
        } else {
            sendMessage(session, new GameMessage(GameAction.wrongColor, null));
        }
    }


    private static Optional<User> lookupUser(Session session) {
        return users.stream().filter(u -> u.session.equals(session)).findFirst();
    }

    private static GameMessage userList(GameAction action) {
        return new GameMessage(action, userNames());
    }

    private static List<String> userNames() {
        return users.stream().map(u -> u.name).collect(Collectors.toList());
    }
    */
}
