package com.example.bean;

import org.eclipse.jetty.websocket.api.Session;

public class Player {

    private final transient Session session;

    private String name;
    private int color;
    private boolean ready;

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

    public boolean remainingCellCountIs(int remainingCellCount) {
        return this.cellCount == remainingCellCount;
    }

    public void decrementCellCount() {
        cellCount--;
    }

    public void setInitialCellCount(int cellCount) {
        this.cellCount = cellCount;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getCellCount() {
		return cellCount;
	}

	public void setCellCount(int cellCount) {
		this.cellCount = cellCount;
	}

	public Session getSession() {
		return session;
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
