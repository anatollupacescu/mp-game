package com.example;

import com.example.bean.Game;
import com.example.bean.GameAction;
import com.example.bean.GameMessage;
import com.example.bean.Player;
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
    private static final AtomicInteger playerCount = new AtomicInteger(0);
    private static final Gson mapper = new GsonBuilder().create();
    private static final List<Player> players = new ArrayList<>();

    enum PlayerEvent {add, remove, setReady}

    static {

        bus.on($("sendMessage"), (Event<Tuple2<List<Player>, GameMessage>> event) ->
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

        bus.receive($("updatePlayerList"), (Event<Tuple2<PlayerEvent, Player>> ev) -> {
            Player player = ev.getData().getT2();
            switch (ev.getData().getT1()) {
                case add:
                    players.add(player);
                    return players;
                case remove:
                    players.remove(player);
                    return players;
                case setReady:
                    player.isReady(true);
                    return players;
                default:
                    break;
            }
            throw new IllegalStateException();
        });

        bus.on($("game"), new Consumer<Event<Tuple2<List<Player>, GameMessage>>>() {

            private Game game = null;
            private List<Player> playerList;

            @Override public void accept(Event<Tuple2<List<Player>, GameMessage>> ev) {
                GameMessage gameMsg = ev.getData().getT2();
                switch (gameMsg.getAction()) {
                    case startGame:
                        this.playerList = ev.getData().getT1();
                        game = new Game(playerList);
                        List<Integer> colorsArray = game.colorsArray();
                        GameMessage colorsArrayGameMsg = new GameMessage(GameAction.startGame, colorsArray.toString());
                        bus.notify("sendMessage", Event.wrap(Tuple2.of(playerList, colorsArrayGameMsg)));
                        break;
                    case cellClick:
                        if(game == null) return;
                        Player player = ev.getData().getT1().iterator().next();
                        Double cellId = gameMsg.getData(Double.class);
                        if(game.markCell(player, cellId)) {
                            bus.notify("sendMessage", Event.wrap(Tuple2.of(playerList, gameMsg)));
                        }
                        game.getWinner().ifPresent(winner -> {
                            GameMessage gm = new GameMessage(GameAction.winner, winner.name);
                            game = null;
                            bus.notify("sendMessage", Event.wrap(Tuple2.of(playerList, gm)));
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
                playerLogIn(session, gameMessage);
                break;
            case ready:
                playerReady(session, gameMessage);
                break;
            case cellClick:
                 cellClick(session, gameMessage);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void sessionCreated(Session session) {
        GameMessage gameMessage = new GameMessage(GameAction.connect, players.toArray());
        List<Player> list = ImmutableList.of(new Player(session, 0, null));
        Tuple2<List<Player>, GameMessage> tuple = Tuple2.of(list, gameMessage);
        bus.notify("sendMessage", Event.wrap(tuple));
    }

    private static void broadcastUserList(List<Player> players) {
        GameMessage message = new GameMessage(GameAction.logIn, players.toArray());
        bus.notify("sendMessage", Event.wrap(Tuple2.of(players, message)));
    }

    private static void playerLogIn(Session session, GameMessage gameMessage) {
        Player player = new Player(session, playerCount.incrementAndGet(), gameMessage.getData(String.class));
        bus.sendAndReceive("updatePlayerList", Event.wrap(Tuple2.of(PlayerEvent.add, player)), event -> {
            List<Player> playerList = (List<Player>) event.getData();
            broadcastUserList(playerList);
        });
    }

    public static void playerDisconnect(Session session) {
        Optional<Player> player = getPlayerBySession(session);
        bus.sendAndReceive("updatePlayerList", Event.wrap(Tuple2.of(PlayerEvent.remove, player.get())), response -> {
            List<Player> playerList = (List<Player>) response.getData();
            broadcastUserList(playerList);
        });
    }

    private static Optional<Player> getPlayerBySession(Session session) {
        return players.stream().filter(u -> u.session.equals(session)).findFirst();
    }

    private static void playerReady(Session session, GameMessage gameMessage) {
        Boolean isReady = gameMessage.getData(Boolean.class);
        if (Boolean.TRUE.equals(isReady)) {
            /* has grant start */
            Optional<Player> player = getPlayerBySession(session);
            bus.sendAndReceive("updatePlayerList", Event.wrap(Tuple2.of(PlayerEvent.setReady, player.get())), response -> {
                final List<Player> playerListList = players.stream().filter(u -> u.ready).collect(Collectors.toList());
                GameMessage startGameMsg = new GameMessage(GameAction.startGame, null);
                bus.notify("game", Event.wrap(Tuple2.of(playerListList, startGameMsg)));
            });
        } else {
            Optional<Player> player = getPlayerBySession(session);
            bus.sendAndReceive("updatePlayerList", Event.wrap(Tuple2.of(PlayerEvent.setReady, player.get())), response -> {
                List<Player> playersNotReadyYet = players.stream().filter(u -> !u.ready).collect(Collectors.toList());
                if (playersNotReadyYet.size() == 1) {
                    Player lastUser = playersNotReadyYet.iterator().next();
                    GameMessage grantStartGameMsg = new GameMessage(GameAction.grantStart, null);
                    bus.notify("sendMessage", Event.wrap(Tuple2.of(ImmutableList.of(lastUser), grantStartGameMsg)));
                }
            });
            GameMessage startGameMsg = new GameMessage(GameAction.ready, null);
            bus.notify("sendMessage", Event.wrap(Tuple2.of(ImmutableList.of(player.get()), startGameMsg)));
        }
    }

    private static void cellClick(Session session, GameMessage gameMessage) {
        Optional<Player> player = getPlayerBySession(session);
        bus.notify("game", Event.wrap(Tuple2.of(ImmutableList.of(player.get()), gameMessage)));
    }
}
