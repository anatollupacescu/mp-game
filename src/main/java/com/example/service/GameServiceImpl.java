package com.example.service;

import com.example.service.bean.game.Game;
import com.example.service.bean.game.GameMessage;
import reactor.core.processor.RingBufferProcessor;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.GameService;

public class GameServiceImpl implements GameService {

    private final RingBufferProcessor<Tuple2<Player, GameMessage<?>>> processor = RingBufferProcessor.create();
	private final Stream<Tuple2<Player, GameMessage<?>>> stream = Streams.wrap(processor);

    private Game game;

    @Override
	public Player getWinner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startGame() {
		stream.consume(playerGameMessageTuple2 -> {

        });
	}

	@Override
	public void stopGame() {
        processor.onNext(Tuple2.of(null, GameMessage.stopGame()));
	}

	@Override
	public Object[] getGameData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGameRunning() {
		return game != null;
	}

    @Override
    public void markCell(Player user, Cell cell) {
		processor.onNext(Tuple2.of(user, GameMessage.markCell(cell)));
    }
}
