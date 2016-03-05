package com.example.service.bean.game;

import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Game {

	final int size = 8;

	private final List<Cell> cells;
	private final List<Player> players;

	public Game(List<Player> players) {
		this.cells = new LinkedList<>();
		this.players = players;

		int cellPerColor = (size * size) / (players.size() + 1);

		players.stream().forEach(player -> {
			for (int j = 0; j < cellPerColor; j++) {
				Cell cell = new Cell(player);
				cells.add(cell);
			}
			player.setInitialCellCount(cellPerColor);
		});

		for (int i = cells.size(); i < size * size; i++) {
			cells.add(new Cell(null));
		}
		Collections.shuffle(cells);
	}

	public void markCell(Cell c) {
		getCells().stream().filter(cell -> cell.equals(c)).forEach(cell -> cell.check());
	}

	public Optional<Player> getWinner() {
		return players.stream().filter(player -> player.getCellCount() == 0).findFirst();
	}

	public List<Integer> colorsArray() {
		return cells.stream().mapToInt(cell -> {
			if (cell.getPlayer() == null) {
				return 0;
			}
			return cell.getPlayer().getColor();
		}).boxed().collect(Collectors.toList());
	}

	public Optional<Cell> getCellByIndex(int index) {
		return Optional.ofNullable(cells.get(index));
	}
	
	public List<Cell> getCells() {
		return cells;
	}
}
