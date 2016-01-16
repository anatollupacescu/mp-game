package com.example.bean;

public class Cell {

    private User user;

    private boolean checked;

    public Cell() {
    }

    public boolean isChecked() {
        return checked;
    }

    public void check() {
        this.checked = true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
