package com.example.service.bean.game;

/**
 * Created by anatol on 18/02/16.
 */
public class GameMessage<T> {

    private final GameAction action;

    private final String data;

    public GameMessage(GameAction action, String data) {
        this.action = action;
        this.data = data;
    }

    public GameAction getAction() {
        return action;
    }

    public String getData() {
        return data;
    }
}
