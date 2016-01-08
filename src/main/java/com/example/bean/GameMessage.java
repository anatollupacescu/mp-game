package com.example.bean;

public class GameMessage {

    public GameMessage(GameAction action, Object data) {
        this.action = action;
        this.data = data;
    }

    private GameAction action;

    private Object data;

    public GameAction getAction() {
        return action;
    }

    public void setAction(GameAction action) {
        this.action = action;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
