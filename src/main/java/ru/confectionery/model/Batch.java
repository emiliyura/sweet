package ru.confectionery.model;

import org.bson.types.ObjectId;
import java.util.Date;

public class Batch {
    private ObjectId id;
    private ObjectId productId;
    private int quantity;
    private Date productionDate;
    private Date expiryDate;
    private String batchNumber;
    private String status;
    
    public Batch() {}
    
    public Batch(ObjectId productId, int quantity, Date productionDate, Date expiryDate, String batchNumber, String status) {
        this.productId = productId;
        this.quantity = quantity;
        this.productionDate = productionDate;
        this.expiryDate = expiryDate;
        this.batchNumber = batchNumber;
        this.status = status;
    }
    
    // Геттеры и сеттеры
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public ObjectId getProductId() { return productId; }
    public void setProductId(ObjectId productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 