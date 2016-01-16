package com.example;

import com.example.bean.Game;
import com.example.bean.GameAction;
import com.example.bean.GameMessage;
import com.example.bean.User;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.websocket.api.Session;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import reactor.fn.tuple.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static reactor.bus.selector.Selectors.$;

public class GameService {

    private static final EventBus bus = EventBus.create();
    private static final AtomicInteger userCount = new AtomicInteger(0);
    private static final Gson mapper = new GsonBuilder().create();
    private static final List<User> players = new ArrayList<>();

    enum UserEventType {addUser, removeUser, setUserReady}

    static {

        bus.on($("sendMessage"), (Event<Tuple2<List<User>, GameMessage>> event) ->
                event.getData().getT1().stream().forEach(u -> {
                    String str = mapper.toJson(event.getData().getT2());
                    try {
                        if (u.session.isOpen()) {
                            u.session.getRemote().sendString(str);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                })
        );

        bus.receive($("userList"), (Event<Tuple2<UserEventType, User>> ev) -> {
            User user = ev.getData().getT2();
            switch (ev.getData().getT1()) {
                case addUser:
                    players.add(user);
                    return players;
                case removeUser:
                    players.remove(user);
                    return players;
                case setUserReady:
                    user.isReady(true);
                    return players;
                default:
                    break;
            }
            throw new IllegalStateException();
        });

        bus.on($("game"), new Consumer<Event<Tuple2<List<User>, GameMessage>>>() {

            private Game game = null;
            private List<User> userList;

            @Override public void accept(Event<Tuple2<List<User>, GameMessage>> ev) {
                GameMessage gameMsg = ev.getData().getT2();
                switch (gameMsg.getAction()) {
                    case startGame:
                        this.userList = ev.getData().getT1();
                        game = new Game(userList);
                        List<Integer> colorsArray = game.colorsArray();
                        GameMessage colorsArrayGameMsg = new GameMessage(GameAction.startGame, colorsArray.toString());
                        bus.notify("sendMessage", Event.wrap(Tuple2.of(userList, colorsArrayGameMsg)));
                        break;
                    case cellClick:
                        if(game == null) return;
                        User userOpt = ev.getData().getT1().iterator().next();
                        Double cellId = gameMsg.getData(Double.class);
                        if(game.markCell(userOpt, cellId)) {
                            bus.notify("sendMessage", Event.wrap(Tuple2.of(userList, gameMsg)));
                        }
                        game.getWinner().ifPresent(winner -> {
                            GameMessage gm = new GameMessage(GameAction.winner, winner.name);
                            game = null;
                            bus.notify("sendMessage", Event.wrap(Tuple2.of(userList, gm)));
                        });
                        break;
                    default:
                        throw new IllegalStateException("Unknown action");
                }
            }});
    }

    public static void handleClientMessage(String message, Session session) {
        GameMessage gameMessage = mapper.fromJson(message, GameMessage.class);
        switch (gameMessage.getAction()) {
            case logIn:
                userLogIn(session, gameMessage);
                break;
            case ready:
                userReady(session, gameMessage);
                break;
            case cellClick:
                 cellClick(session, gameMessage);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void sessionCreated(Session session) {
        GameMessage gameMessage = userNameList(GameAction.connect, players);
        List<User> list = ImmutableList.of(new User(session, 0, null));
        Tuple2<List<User>, GameMessage> tuple = Tuple2.of(list, gameMessage);
        bus.notify("sendMessage", Event.wrap(tuple));
    }

    private static GameMessage userNameList(GameAction action, List<User> users) {
        Object data = users.stream().map(u -> u.name).collect(Collectors.toList());
        return new GameMessage(action, data);
    }

    private static void broadcastUserList(List<User> userList) {
        GameMessage message = userNameList(GameAction.logIn, userList);
        bus.notify("sendMessage", Event.wrap(Tuple2.of(userList, message)));
    }

    private static void userLogIn(Session session, GameMessage gameMessage) {
        User player = new User(session, userCount.incrementAndGet(), gameMessage.getData(String.class));
        bus.sendAndReceive("userList", Event.wrap(Tuple2.of(UserEventType.addUser, player)), event -> {
            List<User> userList = (List<User>) event.getData();
            broadcastUserList(userList);
        });
    }

    public static void userDisconnect(Session session) {
        Optional<User> player = getUser(session);
        bus.sendAndReceive("userList", Event.wrap(Tuple2.of(UserEventType.removeUser, player.get())), response -> {
            List<User> userList = (List<User>) response.getData();
            broadcastUserList(userList);
        });
    }

    private static Optional<User> getUser(Session session) {
        return players.stream().filter(u -> u.session.equals(session)).findFirst();
    }

    private static void userReady(Session session, GameMessage gameMessage) {
        Boolean isReady = gameMessage.getData(Boolean.class);
        if (Boolean.TRUE.equals(isReady)) {
            /* has grant start */
            Optional<User> user = getUser(session);
            bus.sendAndReceive("userList", Event.wrap(Tuple2.of(UserEventType.setUserReady, user.get())), response -> {
                final List<User> userList = players.stream().filter(u -> u.ready).collect(Collectors.toList());
                GameMessage startGameMsg = new GameMessage(GameAction.startGame, null);
                bus.notify("game", Event.wrap(Tuple2.of(userList, startGameMsg)));
            });
        } else {
            Optional<User> user = getUser(session);
            bus.sendAndReceive("userList", Event.wrap(Tuple2.of(UserEventType.setUserReady, user.get())), response -> {
                List<User> usersNotReadyYet = players.stream().filter(u -> !u.ready).collect(Collectors.toList());
                if (usersNotReadyYet.size() == 1) {
                    User lastUser = usersNotReadyYet.iterator().next();
                    GameMessage grantStartGameMsg = new GameMessage(GameAction.grantStart, null);
                    bus.notify("sendMessage", Event.wrap(Tuple2.of(ImmutableList.of(lastUser), grantStartGameMsg)));
                }
            });
            GameMessage startGameMsg = new GameMessage(GameAction.ready, null);
            bus.notify("sendMessage", Event.wrap(Tuple2.of(ImmutableList.of(user.get()), startGameMsg)));
        }
    }

    private static void cellClick(Session session, GameMessage gameMessage) {
        Optional<User> userClickedCell = getUser(session);
        bus.notify("game", Event.wrap(Tuple2.of(ImmutableList.of(userClickedCell.get()), gameMessage)));
    }
}
