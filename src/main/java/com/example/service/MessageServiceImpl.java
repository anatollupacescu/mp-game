package com.example.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.processor.RingBufferProcessor;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.action.Control;
import com.example.service.bean.client.ClientAction;
import com.example.service.bean.client.ClientMessage;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.MessageService;

public class MessageServiceImpl implements MessageService {

	private final RingBufferProcessor<Tuple2<Player, ClientMessage<?>>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<Player, ClientMessage<?>>> stream = Streams.wrap(processor);
	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ClientMessage<List<Player>> message = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(Object[] gameData) {
		ClientMessage<Object[]> message = ClientMessage.create(ClientAction.gameData, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(Cell cell) {
		ClientMessage<Cell> message = ClientMessage.create(ClientAction.markedCell, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ClientMessage<Player> message = ClientMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public Control registerSession(Player player) {
		return stream.consume(message -> {
			Player destination = message.getT1();
			ClientMessage<?> clientMessage = message.getT2();
			if (destination == null || player.getName().equals(destination.getName())) {
				Session session = destination.getSession();
				sendMessage(session, clientMessage);
			}
		});
	}

    @Override
	public void alert(Player player, String message) {
		processor.onNext(Tuple2.of(player, ClientMessage.createAlert(message)));
	}

    @Override
	public void log(Player player, String message) {
		processor.onNext(Tuple2.of(player, ClientMessage.createLog(message)));
	}

	@Override
	public void sendPlayerList(Session session, List<Player> playerList) {
		ClientMessage playerListMessage = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(playerListMessage);
	}

	private void broadcast(ClientMessage<?> message) {
		processor.onNext(Tuple2.of(null, message));
	}

	private void sendMessage(Session session, ClientMessage<?> clientMessage) {
		try {
			String message = mapper.writeValueAsString(clientMessage);
			if (session.isOpen()) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
}
