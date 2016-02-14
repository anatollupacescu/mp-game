package com.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class DemoApplication {

    /**
     * @param args Command line args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        Server server = context.getBean(Server.class);
        server.start();
        server.join();
    }

    @Configuration
    public static class AppConfiguration {

        public
        @Bean
        Server server() {
            Server server = new Server(8090);

            ServletContextHandler ctx = new ServletContextHandler();
            ctx.setContextPath("/");
			ctx.addServlet(MpGameServiceSocketServlet.class, "/mpgame");

            server.setHandler(ctx);

            return server;
        }

        public static class MpGameServiceSocketServlet extends WebSocketServlet {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MPGameWebSocket.class);
            }
        }
        
        @WebSocket
        class MPGameWebSocket {

            private Session session;

            @OnWebSocketMessage
            public void handleMessage(String message) {
                GatewayService.handleClientMessage(message, session);
            }

            @OnWebSocketConnect
            public void handleConnect(Session session) {
                this.session = session;
                GatewayService.sessionCreated(session);
            }

            @OnWebSocketClose
            public void handleClose(int statusCode, String reason) {
                GatewayService.playerDisconnect(session);
            }

            @OnWebSocketError
            public void handleError(Throwable error) {
                error.printStackTrace();
            }
        }
    }
}
