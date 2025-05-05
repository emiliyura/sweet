package ru.confectionery.model;

import org.bson.types.ObjectId;
import java.util.List;
import java.util.Map;

public class Recipe {
    private ObjectId id;
    private ObjectId productId;
    private Map<String, Double> ingredients; // Имя ингредиента -> количество
    private String instructions;
    
    public Recipe() {}
    
    public Recipe(ObjectId productId, Map<String, Double> ingredients, String instructions) {
        this.productId = productId;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }
    
    // Геттеры и сеттеры
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public ObjectId getProductId() { return productId; }
    public void setProductId(ObjectId productId) { this.productId = productId; }
    
    public Map<String, Double> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, Double> ingredients) { this.ingredients = ingredients; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
} 