package ru.confectionery.model;

import org.bson.types.ObjectId;
import java.util.Date;
import java.util.Map;

public class Order {
    private ObjectId id;
    private String customerName;
    private String customerContact;
    private Map<ObjectId, Integer> products; // ProductId -> количество
    private Date orderDate;
    private Date deliveryDate;
    private String status;
    private double totalAmount;
    
    public Order() {}
    
    public Order(String customerName, String customerContact, Map<ObjectId, Integer> products, 
                 Date orderDate, Date deliveryDate, String status, double totalAmount) {
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.products = products;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }
    
    // Геттеры и сеттеры
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerContact() { return customerContact; }
    public void setCustomerContact(String customerContact) { this.customerContact = customerContact; }
    
    public Map<ObjectId, Integer> getProducts() { return products; }
    public void setProducts(Map<ObjectId, Integer> products) { this.products = products; }
    
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
} 