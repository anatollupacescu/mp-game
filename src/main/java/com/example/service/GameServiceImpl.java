package com.example.service;

import java.util.List;
import java.util.Optional;

import com.example.bean.game.Game;

import skeleton.bean.Cell;
import skeleton.bean.Player;
import skeleton.service.GameService;

public class GameServiceImpl implements GameService {

	private Game game;

	@Override
	public Optional<Player> getWinner() {
		return game.getWinner();
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
	public int markCell(Cell cell) {
		return game.markCell(cell);
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

	@Override
	public void dropPlayer(Player player) {
		game.dropPlayer(player);
	}
}
