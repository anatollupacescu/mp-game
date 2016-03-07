package com.example.bean.client;

public class ClientMessage {

	private ClientAction action;
	private String value;

	public ClientMessage() {
	}
	
	public ClientMessage(ClientAction action, String value) {
		this.action = action;
		this.value = value;
	}

	public ClientAction getAction() {
		return action;
	}

	public String getValue() {
		return value;
	}

	public void setAction(ClientAction action) {
		this.action = action;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static ClientMessage create(ClientAction action, String value) {
		return new ClientMessage(action, value);
	}

	public static ClientMessage createAlert(String value) {
		return new ClientMessage(ClientAction.alert, value);
	}

	public static ClientMessage createLog(String string) {
		return new ClientMessage(ClientAction.log, string);
	}
}
