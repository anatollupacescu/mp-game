package com.example;

import static reactor.bus.selector.Selectors.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jetty.websocket.api.Session;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import reactor.fn.tuple.Tuple2;
import skeleton.bean.game.Game;
import skeleton.bean.player.Player;

public class OldGameService {

    private static final String GAME = "game";
	private static final String PLAYER_LIST = "updatePlayerList";
	private static final String BROADCAST_MESSAGE = "sendMessage";
	
	private static final EventBus bus = EventBus.create();
    private static final Gson mapper = new GsonBuilder().create();
    private static final List<Player> players = new ArrayList<>();
    private static final Queue<Integer> colors = Lists.newLinkedList(IntStream.rangeClosed(1, 7).boxed().collect(Collectors.toList()));

    enum PlayerEvent {add, remove, setReady}

    static {

        bus.on($(BROADCAST_MESSAGE), (Event<GameMessage> event) ->
            players.stream().forEach(u -> {
                String str = mapper.toJson(event.getData());
                try {
                    if (u.getSession().isOpen()) {
                        u.getSession().getRemote().sendString(str);
                    }
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            })
        );

        bus.receive($(PLAYER_LIST), (Event<Tuple2<PlayerEvent, Player>> ev) -> {
            Player player = ev.getData().getT2();
            switch (ev.getData().getT1()) {
                case add:
                    player.setColor(colors.poll());
                    players.add(player);
                    return players;
                case remove:
                    players.remove(player);
                    colors.add(player.getColor());
                    return players;
                case setReady:
                    player.setReady(true);
                    return players;
                default:
                    break;
            }
            throw new IllegalStateException();
        });

        bus.on($(GAME), new Consumer<Event<Tuple2<List<Player>, GameMessage>>>() {

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
                        bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(playerList, colorsArrayGameMsg)));
                        break;
                    case cellClick:
                        if(game == null) return;
                        Player player = ev.getData().getT1().iterator().next();
                        Double cellId = gameMsg.getData(Double.class);
                        if(game.markCell(player, cellId)) {
                            bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(playerList, gameMsg)));
                        }
                        game.getWinner().ifPresent(winner -> {
                            GameMessage gm = new GameMessage(GameAction.winner, winner.getName());
                            game = null;
                            bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(playerList, gm)));
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
        List<Player> list = ImmutableList.of(new Player(session));
        Tuple2<List<Player>, GameMessage> tuple = Tuple2.of(list, gameMessage);
        bus.notify(BROADCAST_MESSAGE, Event.wrap(tuple));
    }

    private static void broadcastUserList(List<Player> players) {
        GameMessage message = new GameMessage(GameAction.logIn, players.toArray());
        bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(players, message)));
    }

    private static void playerLogIn(Session session, GameMessage gameMessage) {
        Player player = new Player(session, gameMessage.getData(String.class));
        bus.sendAndReceive(PLAYER_LIST, Event.wrap(Tuple2.of(PlayerEvent.add, player)), event -> {
        	@SuppressWarnings("unchecked")
            List<Player> playerList = (List<Player>) event.getData();
            broadcastUserList(playerList);
        });
    }

    public static void playerDisconnect(Session session) {
        Optional<Player> player = getPlayerBySession(session);
        bus.sendAndReceive(PLAYER_LIST, Event.wrap(Tuple2.of(PlayerEvent.remove, player.get())), response -> {
            @SuppressWarnings("unchecked")
			List<Player> playerList = (List<Player>) response.getData();
            broadcastUserList(playerList);
        });
    }

    private static Optional<Player> getPlayerBySession(Session session) {
        return players.stream().filter(u -> u.getSession().equals(session)).findFirst();
    }

    private static void playerReady(Session session, GameMessage gameMessage) {
        Boolean isReady = gameMessage.getData(Boolean.class);
        if (Boolean.TRUE.equals(isReady)) {
            /* has grant start */
            Optional<Player> player = getPlayerBySession(session);
            bus.sendAndReceive(PLAYER_LIST, Event.wrap(Tuple2.of(PlayerEvent.setReady, player.get())), response -> {
                final List<Player> playerListList = players.stream().filter(u -> u.isReady()).collect(Collectors.toList());
                GameMessage startGameMsg = new GameMessage(GameAction.startGame, null);
                bus.notify(GAME, Event.wrap(Tuple2.of(playerListList, startGameMsg)));
            });
        } else {
            Optional<Player> player = getPlayerBySession(session);
            bus.sendAndReceive(PLAYER_LIST, Event.wrap(Tuple2.of(PlayerEvent.setReady, player.get())), response -> {
                List<Player> playersNotReadyYet = players.stream().filter(u -> !u.isReady()).collect(Collectors.toList());
                if (playersNotReadyYet.size() == 1) {
                    Player lastUser = playersNotReadyYet.iterator().next();
                    GameMessage grantStartGameMsg = new GameMessage(GameAction.grantStart, null);
                    bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(ImmutableList.of(lastUser), grantStartGameMsg)));
                }
            });
            GameMessage startGameMsg = new GameMessage(GameAction.ready, null);
            bus.notify(BROADCAST_MESSAGE, Event.wrap(Tuple2.of(ImmutableList.of(player.get()), startGameMsg)));
        }
    }

    private static void cellClick(Session session, GameMessage gameMessage) {
        getPlayerBySession(session).ifPresent(player -> {
        	bus.notify(GAME, Event.wrap(Tuple2.of(ImmutableList.of(player), gameMessage)));
        });
    }
}
