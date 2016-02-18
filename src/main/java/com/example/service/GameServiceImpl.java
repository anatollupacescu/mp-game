package com.example.service;

import com.example.service.bean.game.GameMessage;
import reactor.core.processor.RingBufferProcessor;
import reactor.fn.tuple.Tuple2;
import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.GameService;

import java.util.Optional;

public class GameServiceImpl implements GameService {

    private final RingBufferProcessor<Tuple2<Player, GameMessage<?>>> processor = RingBufferProcessor.create();

    @Override
	public Player getWinner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean startGame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopGame() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getGameData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGameRunning() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public void markCell(Player user, Cell cell) {

    }

    @Override
	public Optional<Cell> getCellById(String value) {
		// TODO Auto-generated method stub
		return null;
	}
}
