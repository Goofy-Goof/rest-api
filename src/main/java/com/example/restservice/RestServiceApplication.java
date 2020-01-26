package com.example.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings( "deprecated" )
@SpringBootApplication
public class RestServiceApplication {

    public static void main(String[] args) {

        RequestController controler = RequestController.RequestControler();
        SpringApplication.run(RestServiceApplication.class, args);




    }

}
