package jcorechat.app_api.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jcorechat.app_api.API;

class MongoDB {

    private final String mongo_database = "jcorechat";
    private final String mongo_url = "mongodb://localhost:27017";
    private MongoClient mongoClient = null;

    MongoDB() {
        try {
            mongoClient = MongoClients.create(mongo_url);
            // Access a specific database
            MongoDatabase database = mongoClient.getDatabase(mongo_database);

            // Now you can perform operations on the database
            API.logger.info("Mongo DB Connected to database: " + database.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
