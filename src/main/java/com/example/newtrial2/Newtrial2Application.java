package com.example.newtrial2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.newtrial2.mapper")
public class Newtrial2Application {

    public static void main(String[] args) {
        SpringApplication.run(Newtrial2Application.class, args);
    }

}