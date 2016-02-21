package com.example.service.bean.game;

import skeleton.bean.game.Cell;

public class GameMessage<T> {

    private final GameAction action;

    private final T data;

    public GameMessage(GameAction action, T data) {
        this.action = action;
        this.data = data;
    }

    public GameAction getAction() {
        return action;
    }

    public T getData() {
        return data;
    }

    public static GameMessage<Cell> markCell(Cell cell) {
        return new GameMessage(GameAction.cellClick, cell);
    }

    public static GameMessage stopGame() {
        return new GameMessage(GameAction.stopGame, null);
    }
}
