package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.bean.game.Game;

import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;
import skeleton.service.GameService;

public class GameServiceImpl implements GameService {

	private Game game;

	@Override
	public Player getWinner() {
		return game.getWinner().get();
	}

	@Override
	public void startGame(List<Player> playerList) {
		game = new Game(playerList);
	}

	@Override
	public void stopGame() {
		game = null;
	}

	@Override
	public List<Cell> getGameData() {
		return game.getCells();
	}

	@Override
	public boolean isGameRunning() {
		return game != null;
	}

	@Override
	public void markCell(Cell cell) {
		game.markCell(cell);
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
