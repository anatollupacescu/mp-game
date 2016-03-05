package com.example.service.async;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.client.ClientAction;
import com.example.bean.client.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.processor.RingBufferProcessor;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.action.Control;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.MessageService;

public class MessageServiceAsyncImpl implements MessageService {

	private final RingBufferProcessor<Tuple2<Player, ClientMessage<?>>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<Player, ClientMessage<?>>> stream = Streams.wrap(processor);
	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ClientMessage<List<Player>> message = ClientMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(List<Cell> gameData) {
		ClientMessage<List<Cell>> message = ClientMessage.create(ClientAction.gameData, gameData);
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
		ClientMessage<List<Player>> playerListMessage = ClientMessage.create(ClientAction.playerList, playerList);
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

	@Override
	public void alert(Session session, String message) {
		ClientMessage<String> clientMessage = ClientMessage.createAlert(message);
		sendMessage(session, clientMessage);
	}

	@Override
	public void log(Session session, String message) {
		ClientMessage<String> clientMessage = ClientMessage.createLog(message);
		sendMessage(session, clientMessage);
	}
}
