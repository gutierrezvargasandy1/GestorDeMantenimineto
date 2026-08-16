package com.utng.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URI = dotenv.get("URL_MONGO");
    private static final String DATABASE_NAME = dotenv.get("NAME_DATABASE_MONGO");

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private MongoDBConnection() {
    }

    public static MongoDatabase getDatabase() {

        if (mongoClient == null) {
            mongoClient = MongoClients.create(URI);
            database = mongoClient.getDatabase(DATABASE_NAME);

            System.out.println("Conexión establecida con MongoDB");
        }

        return database;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
        }
    }
}