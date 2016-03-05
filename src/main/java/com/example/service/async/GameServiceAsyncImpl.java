package com.example.service.async;

import java.util.List;
import java.util.Optional;

import com.example.service.bean.game.Game;
import com.example.service.bean.game.GameMessage;

import reactor.core.processor.RingBufferProcessor;
import reactor.rx.Stream;
import reactor.rx.Streams;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.GameService;

public class GameServiceAsyncImpl implements GameService {

	private final RingBufferProcessor<GameMessage<?>> processor = RingBufferProcessor.create();
	private final Stream<GameMessage<?>> stream = Streams.wrap(processor);
	private Game game;

	{
		stream.consume(gameMessage -> {
			switch (gameMessage.action) {
			case startGame:
				break;
			default:
				throw new IllegalStateException();
			}
		});
	}

	@Override
	public Player getWinner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startGame(List<Player> playerList) {
		processor.onNext(GameMessage.startGame(playerList));
	}

	@Override
	public void stopGame() {
		processor.onNext(GameMessage.stopGame());
	}

	@Override
	public List<Cell> getGameData() {
		return null;
	}

	@Override
	public boolean isGameRunning() {
		return game != null;
	}

	@Override
	public void markCell(Cell cell) {
		processor.onNext(GameMessage.markCell(cell));
	}

	@Override
	public Optional<Cell> getCellByIndex(String id) {
		int index;
		try {
			index = Integer.valueOf(id);
		} catch(Exception e) {
			return Optional.empty();
		}
		return game.getCellByIndex(index);
	}
}
