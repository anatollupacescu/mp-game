package com.example.bean;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

public class User {

	public final Session session;
	
	public final String name;

	public User(Session session, String name) {
		super();
		this.session = session;
		this.name = name;
	}

	public void sendMessage(String str) {
		try {
			if (session.isOpen()) {
				session.getRemote().sendString(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
