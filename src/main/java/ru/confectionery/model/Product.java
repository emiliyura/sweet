package ru.confectionery.model;

import org.bson.types.ObjectId;

public class Product {
    private ObjectId id;
    private String name;
    private String type;
    private double price;
    private int weight;
    private String description;
    
    public Product() {}
    
    public Product(String name, String type, double price, int weight, String description) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.weight = weight;
        this.description = description;
    }
    
    // Геттеры и сеттеры
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() {
        return name + " (" + type + ") - " + price + " руб.";
    }
} 