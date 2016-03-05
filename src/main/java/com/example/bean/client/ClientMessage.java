package com.example.bean.client;

public class ClientMessage<T> {

	private ClientAction action;
	private T value;

	private ClientMessage(ClientAction action, T value) {
		super();
		this.action = action;
		this.value = value;
	}

	public ClientAction getAction() {
		return action;
	}

	public Object getValue() {
		return value;
	}

	public T getValue(Class<T> clazz) {
		return value;
	}

	public static <T> ClientMessage<T> create(ClientAction action, T value) {
		return new ClientMessage<T>(action, value);
	}

	public static <T> ClientMessage<T> createAlert(T value) {
		return new ClientMessage<T>(ClientAction.alert, value);
	}

	public static <T> ClientMessage<T> createLog(T string) {
		return new ClientMessage<T>(ClientAction.log, string);
	}
}
