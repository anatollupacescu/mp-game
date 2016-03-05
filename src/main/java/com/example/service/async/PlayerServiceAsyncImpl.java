package com.example.service.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.websocket.api.Session;

import reactor.core.processor.RingBufferProcessor;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;
import skeleton.bean.player.Player;
import skeleton.service.PlayerService;

public class PlayerServiceAsyncImpl implements PlayerService {

	private final RingBufferProcessor<Tuple2<PlayerAction, Player>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<PlayerAction, Player>> stream = Streams.wrap(processor);
	private final List<Player> playerList = new ArrayList<>();
	
	{
		stream.consume(tuple -> {
			Player player = tuple.getT2();
			switch(tuple.getT1()) {
			case addPlayer:
				playerList.add(player);
				break;
			case removePlayer:
				playerList.remove(player);
				break;
			default:
				throw new IllegalStateException();
			}
		});
	}

	@Override
	public Player addPlayer(Session session, String name) {
		Player player = new Player(session, name);
		processor.onNext(Tuple2.of(PlayerAction.addPlayer, player));
		return player;
	}

	@Override
	public void removePlayer(Player player) {
		processor.onNext(Tuple2.of(PlayerAction.removePlayer, player));
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
