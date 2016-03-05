package com.example.service.bean.game;

import java.util.List;

import skeleton.bean.game.Cell;
import skeleton.bean.player.Player;

public class GameMessage<T> {

    public final GameAction action;

    private final T data;

    public GameMessage(GameAction action, T data) {
        this.action = action;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public static GameMessage<Cell> markCell(Cell cell) {
        return new GameMessage(GameAction.cellClick, cell);
    }

    public static GameMessage<?> stopGame() {
        return new GameMessage(GameAction.stopGame, null);
    }

	public static GameMessage<?> startGame(List<Player> playerList) {
		return new GameMessage(GameAction.startGame, playerList);
	}
}
