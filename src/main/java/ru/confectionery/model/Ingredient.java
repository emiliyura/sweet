package ru.confectionery.model;

import org.bson.types.ObjectId;

public class Ingredient {
    private ObjectId id;
    private String name;
    private String unit;
    private double pricePerUnit;
    private int stockQuantity;
    
    public Ingredient() {}
    
    public Ingredient(String name, String unit, double pricePerUnit, int stockQuantity) {
        this.name = name;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.stockQuantity = stockQuantity;
    }
    
    // Геттеры и сеттеры
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    @Override
    public String toString() {
        return name + " (" + unit + ")";
    }
} 