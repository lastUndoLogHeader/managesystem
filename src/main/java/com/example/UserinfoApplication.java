package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class UserinfoApplication {

    public static void main(String[] args) {

        SpringApplication.run(UserinfoApplication.class, args);
        System.out.println("start successfully!!!");
        System.out.println("start successfully!!!");
        System.out.println("start successfully!!!");
        System.out.println("start successfully!!!");
        System.out.println("start successfully!!!");
    }

}
