package com.example;

import org.eclipse.jetty.server.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.config.AppConfig;

public class DemoApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(AppConfig.class, args);
		Server server = context.getBean(Server.class);
		server.start();
		server.join();
	}
}
