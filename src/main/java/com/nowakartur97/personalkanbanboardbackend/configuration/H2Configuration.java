package com.nowakartur97.personalkanbanboardbackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("local")
public class H2Configuration {

    @Value("${app.h2.port.web}")
    private String h2ConsoleWebPort;
    @Value("${app.h2.port.tcp}")
    private String h2ConsoleTcpPort;

    private org.h2.tools.Server webServer;

    private org.h2.tools.Server tcpServer;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
        this.webServer = org.h2.tools.Server.createWebServer("-webPort", h2ConsoleWebPort, "-tcpAllowOthers").start();
        this.tcpServer = org.h2.tools.Server.createTcpServer("-tcpPort", h2ConsoleTcpPort, "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        this.tcpServer.stop();
        this.webServer.stop();
    }
}
