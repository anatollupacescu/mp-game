package com.example.bean;

import org.eclipse.jetty.websocket.api.Session;

public class User {

    public final Session session;
    public final String name;
    public final int color;

    public boolean ready;
    private int cellCount;

    public User(Session session, int color, String name) {
        super();
        this.session = session;
        this.color = color;
        this.name = name;
    }

    public void isReady(boolean b) {
        this.ready = b;
    }

    public boolean remainingCellCountIs(int remainingCellCount) {
        return this.cellCount == remainingCellCount;
    }

    public void decrementCellCount() {
        cellCount--;
    }

    public void setInitialCellCount(int cellCount) {
        this.cellCount = cellCount;
    }
}
