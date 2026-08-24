package com.veyru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VeyruApplication {

  public static void main(String[] args) {
    SpringApplication.run(VeyruApplication.class, args);
  }
}
