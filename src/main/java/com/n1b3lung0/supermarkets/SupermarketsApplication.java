package com.n1b3lung0.supermarkets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SupermarketsApplication {

  public static void main(String[] args) {
    SpringApplication.run(SupermarketsApplication.class, args);
  }
}
