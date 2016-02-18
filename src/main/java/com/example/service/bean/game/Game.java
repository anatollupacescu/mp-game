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

	private final List<Cell> table;
	private final List<Player> playerList;

	public Game(List<Player> players) {
		this.table = new LinkedList<>();
		this.playerList = players;

		int cellPerColor = (size * size) / (players.size() + 1);

		players.stream().forEach(player -> {
			for (int j = 0; j < cellPerColor; j++) {
				Cell cell = new Cell(player);
				table.add(cell);
			}
			player.setInitialCellCount(cellPerColor);
		});

		for (int i = table.size(); i < size * size; i++) {
			table.add(new Cell(null));
		}
		Collections.shuffle(table);
	}

	public boolean markCell(Player player, Double data) {
		Cell cellAtPosition = table.get(data.intValue());
		if (player.equals(cellAtPosition.getPlayer()) && !cellAtPosition.isChecked()) {
			cellAtPosition.check();
			player.decrementCellCount();
			return true;
		}
		return false;
	}

	public Optional<Player> getWinner() {
		return playerList.stream().filter(player -> player.remainingCellCountIs(0)).findFirst();
	}

	public List<Integer> colorsArray() {
		return table.stream().mapToInt(cell -> {
			if (cell.getPlayer() == null) {
				return 0;
			}
			return cell.getPlayer().getColor();
		}).boxed().collect(Collectors.toList());
	}
}
