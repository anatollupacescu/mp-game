package com.example.bean.game;

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

	public int markCell(Cell c) {
		for(int i = 0; i < cells.size(); i++) {
			Cell localCell = cells.get(i);
			if(localCell.equals(c)) {
				if(!localCell.isChecked()) {
					localCell.check();
					localCell.getPlayer().decrementCellCount();
				}
				return i;
			}
		}
		throw new IllegalStateException("Cell not found");
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

	public void dropPlayer(Player player) {
		players.remove(player);
	}
}
