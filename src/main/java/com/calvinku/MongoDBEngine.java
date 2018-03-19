package com.calvinku;

import com.mongodb.MongoClient;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;

public class MongoDBEngine extends MongoClient {

    private final String database = "books";

    public MongoDBEngine() {
        super("localhost", 27017);
    }

    public MongoCollection<Document> getCollection(String collection) {
        return this.getDatabase(this.database).getCollection(collection);
    }

    public void drop(String collection) {
        super.getDatabase(this.database).getCollection(collection).drop();
    }

    public String getDatabase() {
        return this.database;
    }

    public static ArrayList<Document> get(FindIterable<Document> iterable) {
        ArrayList<Document> list = new ArrayList<>();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            list.add(cursor.next());
        }
        cursor.close();
        return list;
    }


}
