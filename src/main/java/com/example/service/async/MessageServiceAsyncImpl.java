package com.example.service.async;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;

import com.example.bean.ServerMessage;
import com.example.bean.client.ClientAction;
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

	private final RingBufferProcessor<Tuple2<Player, ServerMessage>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<Player, ServerMessage>> stream = Streams.wrap(processor);
	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void broadcastPlayerList(List<Player> playerList) {
		ServerMessage message = ServerMessage.create(ClientAction.playerList, playerList);
		broadcast(message);
	}

	@Override
	public void broadcastGameTable(List<Cell> gameData) {
		ServerMessage message = ServerMessage.create(ClientAction.startGame, gameData);
		broadcast(message);
	}

	@Override
	public void broadcastMarkedCell(int cell) {
		ServerMessage message = ServerMessage.create(ClientAction.cellClick, cell);
		broadcast(message);
	}

	@Override
	public void broadcastWinner(Player winner) {
		ServerMessage message = ServerMessage.create(ClientAction.winner, winner);
		broadcast(message);
	}

	@Override
	public Control registerSession(Player player) {
		return stream.consume(message -> {
			Player destination = message.getT1();
			ServerMessage clientMessage = message.getT2();
			if (destination == null || player.getName().equals(destination.getName())) {
				Session session = destination.getSession();
				sendMessage(session, clientMessage);
			}
		});
	}

    @Override
	public void alert(Player player, String message) {
		processor.onNext(Tuple2.of(player, ServerMessage.createAlert(message)));
	}

    @Override
	public void log(Player player, String message) {
		processor.onNext(Tuple2.of(player, ServerMessage.createLog(message)));
	}

	@Override
	public void sendPlayerList(Session session, List<Player> playerList) {
		ServerMessage playerListMessage = ServerMessage.create(ClientAction.playerList, playerList);
		broadcast(playerListMessage);
	}

	private void broadcast(ServerMessage message) {
		processor.onNext(Tuple2.of(null, message));
	}

	private void sendMessage(Session session, ServerMessage clientMessage) {
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
		ServerMessage clientMessage = ServerMessage.createAlert(message);
		sendMessage(session, clientMessage);
	}

	@Override
	public void log(Session session, String message) {
		ServerMessage clientMessage = ServerMessage.createLog(message);
		sendMessage(session, clientMessage);
	}
}
