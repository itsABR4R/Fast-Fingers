package com.typinggame.multiplayer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Starts the raw socket MultiplayerServer inside the Spring Boot process.
 */
@Component
public class MultiplayerServerBootstrap implements ApplicationRunner {

    private final MultiplayerServer multiplayerServer;

    public MultiplayerServerBootstrap(MultiplayerServer multiplayerServer) {
        this.multiplayerServer = multiplayerServer;
    }

    @Override
    public void run(ApplicationArguments args) {
        Thread t = new Thread(multiplayerServer);
        t.setName("MultiplayerServer-Main");
        t.setDaemon(true); // don't block shutdown
        t.start();
    }
}
