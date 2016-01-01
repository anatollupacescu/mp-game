package com.example.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.StockServiceWebSocket;

@Configuration
public class AppConfiguration {

	public @Bean Server server() {
		Server server = new Server(8090);

		ServletContextHandler ctx = new ServletContextHandler();
		ctx.setContextPath("/");
		ctx.addServlet(StockServiceSocketServlet.class, "/stocks");

		server.setHandler(ctx);

		return server;
	}

	public static class StockServiceSocketServlet extends WebSocketServlet {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.register(StockServiceWebSocket.class);
		}
	}
}
