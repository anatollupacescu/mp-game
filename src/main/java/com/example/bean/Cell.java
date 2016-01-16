package com.example.bean;

public class Cell {

    private Player player;

    private boolean checked;

    public Cell() {
    }

    public boolean isChecked() {
        return checked;
    }

    public void check() {
        this.checked = true;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
