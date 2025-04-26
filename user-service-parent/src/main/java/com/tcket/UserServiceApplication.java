package com.tcket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.tcket","com.tcket.*"})
@EnableAutoConfiguration
@EnableScheduling
public class UserServiceApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
