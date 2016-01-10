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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!session.equals(user.session)) return false;
        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        int result = session.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
