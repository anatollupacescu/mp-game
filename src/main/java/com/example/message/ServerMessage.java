package com.example.message;

public class ServerMessage {

	private ClientAction action;
	private Object value;

	public ServerMessage(ClientAction action, Object value) {
		this.action = action;
		this.value = value;
	}

	public ClientAction getAction() {
		return action;
	}

	public void setAction(ClientAction action) {
		this.action = action;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public static ServerMessage create(ClientAction action, Object value) {
		return new ServerMessage(action, value);
	}

	public static ServerMessage createAlert(Object value) {
		return new ServerMessage(ClientAction.alert, value);
	}

	public static ServerMessage createLog(Object string) {
		return new ServerMessage(ClientAction.log, string);
	}
}
