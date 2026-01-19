package com.typinggame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the MonkeyType-inspired Typing Game.
 */
@SpringBootApplication
public class TypingGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(TypingGameApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("Typing Game API is running!");
        System.out.println("Access the API at: http://localhost:8080/api");
        System.out.println("===========================================\n");
    }
}
