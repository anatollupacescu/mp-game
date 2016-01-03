package com.example.bean;

import org.eclipse.jetty.websocket.api.Session;

public class Game {

	final int size = 8;
	private Session[][] table = new Session[size][size];
	
	public void markCell(Integer cellId, Session session) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				if(cellId.equals((i*size+j))) {
					table[i][j] = session;
				}
			}
		}
	}
}
