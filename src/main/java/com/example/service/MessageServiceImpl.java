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
import skeleton.bean.client.ClientAction;
import skeleton.bean.client.ClientMessage;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.MessageService;

public class MessageServiceImpl implements MessageService {

	private final RingBufferProcessor<Tuple2<Player, ClientMessage>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<Player, ClientMessage>> stream = Streams.wrap(processor);
	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ClientMessage message = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(Object[] gameData) {
		ClientMessage message = ClientMessage.create(ClientAction.gameData, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(Cell cell) {
		ClientMessage message = ClientMessage.create(ClientAction.markedCell, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ClientMessage message = ClientMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public Control registerSession(Player player) {
		return stream.consume(message -> {
			Player destination = message.getT1();
			ClientMessage gameMessage = message.getT2();
			if (destination == null || player.getName().equals(destination.getName())) {
				Session session = destination.getSession();
				sendMessage(session, gameMessage);
			}
		});
	}

	public void broadcast(ClientMessage message) {
		processor.onNext(Tuple2.of(null, message));
	}

	@Override
	public void sendMessage(Session session, ClientMessage clientMessage) {
		try {
			String message = mapper.writeValueAsString(clientMessage);
			if (session.isOpen()) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void sendMessage(Player player, ClientMessage message) {
		processor.onNext(Tuple2.of(player, message));
	}
}