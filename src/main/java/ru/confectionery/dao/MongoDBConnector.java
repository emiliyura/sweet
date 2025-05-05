package ru.confectionery.dao;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.ConnectionString;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongoDBConnector {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    public static void connect() {
        try {
            // Пробуем подключиться через строку подключения
            String connectionString = "mongodb://host.docker.internal:27017,host.docker.internal:27018,host.docker.internal:27019/?replicaSet=rs0";
            mongoClient = MongoClients.create(connectionString);
            
            // Установка таймаута для операций с базой данных
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(5, TimeUnit.SECONDS)
                           .readTimeout(5, TimeUnit.SECONDS))
                .build();
                
            // Если первое подключение не удалось, пробуем через настройки
            if (mongoClient == null) {
                mongoClient = MongoClients.create(settings);
            }
            
            // Подключаемся к базе данных
            database = mongoClient.getDatabase("confectionery");
            
            // Проверяем подключение
            Document result = database.runCommand(new Document("ping", 1));
            System.out.println("Successfully connected to MongoDB: " + result.toJson());
            
            // Создаем коллекции, если их нет
            ensureCollectionsExist();
            
        } catch (MongoException e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
            
            // Пробуем подключиться к одному серверу напрямую
            try {
                mongoClient = MongoClients.create("mongodb://localhost:27017");
                database = mongoClient.getDatabase("confectionery");
                System.out.println("Connected to single MongoDB instance");
                
                // Создаем коллекции, если их нет
                ensureCollectionsExist();
                
            } catch (Exception e2) {
                System.err.println("Failed to connect to single MongoDB instance: " + e2.getMessage());
                e2.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unexpected error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void ensureCollectionsExist() {
        try {
            // Проверяем и создаем необходимые коллекции
            if (database != null) {
                boolean hasProducts = false;
                boolean hasIngredients = false;
                boolean hasRecipes = false;
                boolean hasBatches = false;
                boolean hasOrders = false;
                
                for (String collectionName : database.listCollectionNames()) {
                    if (collectionName.equals("products")) hasProducts = true;
                    if (collectionName.equals("ingredients")) hasIngredients = true;
                    if (collectionName.equals("recipes")) hasRecipes = true;
                    if (collectionName.equals("batches")) hasBatches = true;
                    if (collectionName.equals("orders")) hasOrders = true;
                }
                
                if (!hasProducts) database.createCollection("products");
                if (!hasIngredients) database.createCollection("ingredients");
                if (!hasRecipes) database.createCollection("recipes");
                if (!hasBatches) database.createCollection("batches");
                if (!hasOrders) database.createCollection("orders");
                
                System.out.println("All collections have been created successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to create collections: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
    
    public static MongoCollection<Document> getCollection(String collectionName) {
        if (database == null) {
            System.err.println("Database connection not established. Reconnecting...");
            connect();
            
            if (database == null) {
                System.err.println("Failed to reconnect to database.");
                return null;
            }
        }
        
        try {
            return database.getCollection(collectionName);
        } catch (Exception e) {
            System.err.println("Error getting collection " + collectionName + ": " + e.getMessage());
            return null;
        }
    }
    
    // Общие методы для работы с коллекциями
    public static List<Document> getAllDocuments(String collectionName) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            
            if (collection != null) {
                collection.find().into(documents);
            } else {
                System.err.println("Failed to get collection: " + collectionName);
            }
        } catch (Exception e) {
            System.err.println("Error getting documents from " + collectionName + ": " + e.getMessage());
        }
        
        return documents;
    }
    
    public static Document getDocumentById(String collectionName, ObjectId id) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                return collection.find(new Document("_id", id)).first();
            }
        } catch (Exception e) {
            System.err.println("Error getting document by ID from " + collectionName + ": " + e.getMessage());
        }
        return null;
    }
    
    public static void insertDocument(String collectionName, Document document) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                collection.insertOne(document);
            }
        } catch (Exception e) {
            System.err.println("Error inserting document into " + collectionName + ": " + e.getMessage());
        }
    }
    
    public static void updateDocument(String collectionName, ObjectId id, Document document) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                collection.replaceOne(new Document("_id", id), document);
            }
        } catch (Exception e) {
            System.err.println("Error updating document in " + collectionName + ": " + e.getMessage());
        }
    }
    
    public static void deleteDocument(String collectionName, ObjectId id) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                collection.deleteOne(new Document("_id", id));
            }
        } catch (Exception e) {
            System.err.println("Error deleting document from " + collectionName + ": " + e.getMessage());
        }
    }
    
    public static List<Document> findDocuments(String collectionName, Document filter) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                collection.find(filter).into(documents);
            }
        } catch (Exception e) {
            System.err.println("Error finding documents in " + collectionName + ": " + e.getMessage());
        }
        return documents;
    }
    
    public static List<Document> sortDocuments(String collectionName, String field, int order) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            if (collection != null) {
                collection.find().sort(new Document(field, order)).into(documents);
            }
        } catch (Exception e) {
            System.err.println("Error sorting documents in " + collectionName + ": " + e.getMessage());
        }
        return documents;
    }
} 