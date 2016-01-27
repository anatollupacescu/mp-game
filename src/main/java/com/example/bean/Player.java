package com.example.bean;

import org.eclipse.jetty.websocket.api.Session;

public class Player {

    public final transient Session session;

    public String name;
    public int color;
    public boolean ready;

    private int cellCount;

    public Player(Session session) {
        super();
        this.session = session;
    }

    public Player(Session session, String name) {
        this.session = session;
        this.name = name;
    }

    public Player(Session session, int color, String name) {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Player player = (Player) o;

        if (!session.equals(player.session))
            return false;
        return name.equals(player.name);
    }

    @Override
    public int hashCode() {
        int result = session.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
