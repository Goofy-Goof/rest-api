package com.example.restservice;

import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SuppressWarnings( "deprecated" )
@SpringBootApplication
public class RestServiceApplication {

    public static void main(String[] args) {

        String uri = "mongodb+srv://artem:artem2288@cluster0-i1rs3.mongodb.net/test\n\n";
        MongoClientOptions.Builder options = MongoClientOptions.builder();
        options.sslEnabled(true);
        options.socketKeepAlive(true);
        MongoClientURI clientURI = new MongoClientURI(uri, options);
        MongoClient mongoClient = new MongoClient(clientURI);

        MongoDatabase db = mongoClient.getDatabase("warehouse");
        RequestController.collection = db.getCollection("goods");

        SpringApplication.run(RestServiceApplication.class, args);




    }

}
