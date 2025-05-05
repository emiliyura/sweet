package ru.confectionery.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.confectionery.dao.MongoDBConnector;

public class OrderPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, searchButton, sortButton;
    private JTextField searchField;
    private JComboBox<String> sortField;
    private JComboBox<String> sortOrder;
    
    public OrderPanel() {
        setLayout(new BorderLayout());
        
        // Создание модели таблицы
        String[] columns = {"ID", "Заказчик", "Контакт", "Продукты", "Дата заказа", "Дата доставки", "Статус", "Сумма"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel();
        
        addButton = new JButton("Добавить");
        deleteButton = new JButton("Удалить");
        searchButton = new JButton("Поиск");
        sortButton = new JButton("Сортировать");
        searchField = new JTextField(15);
        
        sortField = new JComboBox<>(new String[]{"customerName", "orderDate", "deliveryDate", "status", "totalAmount"});
        sortOrder = new JComboBox<>(new String[]{"По возрастанию", "По убыванию"});
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(new JLabel("Поиск:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(new JLabel("Сортировать по:"));
        buttonPanel.add(sortField);
        buttonPanel.add(sortOrder);
        buttonPanel.add(sortButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Обработчики событий
        addButton.addActionListener(this::addOrder);
        deleteButton.addActionListener(this::deleteOrder);
        searchButton.addActionListener(this::searchOrders);
        sortButton.addActionListener(this::sortOrders);
        
        // Загрузка данных
        loadData();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Document> orders = MongoDBConnector.getAllDocuments("orders");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : orders) {
            ObjectId id = doc.getObjectId("_id");
            String customerName = doc.getString("customerName");
            String customerContact = doc.getString("customerContact");
            
            Document productsDoc = (Document) doc.get("products");
            StringBuilder productsStr = new StringBuilder();
            if (productsDoc != null) {
                for (String productIdStr : productsDoc.keySet()) {
                    ObjectId productId = new ObjectId(productIdStr);
                    Document productDoc = MongoDBConnector.getDocumentById("products", productId);
                    String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
                    Integer quantity = productsDoc.getInteger(productIdStr);
                    productsStr.append(productName).append(" (").append(quantity).append(" шт.), ");
                }
                if (productsStr.length() > 2) {
                    productsStr.setLength(productsStr.length() - 2);
                }
            }
            
            Date orderDate = doc.getDate("orderDate");
            Date deliveryDate = doc.getDate("deliveryDate");
            String status = doc.getString("status");
            Double totalAmount = doc.getDouble("totalAmount");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                customerName, 
                customerContact, 
                productsStr.toString(), 
                orderDate != null ? dateFormat.format(orderDate) : "", 
                deliveryDate != null ? dateFormat.format(deliveryDate) : "", 
                status,
                totalAmount
            });
        }
    }
    
    private void addOrder(ActionEvent e) {
        // Получаем список продуктов
        List<Document> products = MongoDBConnector.getAllDocuments("products");
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Сначала добавьте продукты!", 
                "Нет продуктов", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JTextField customerNameField = new JTextField();
        JTextField customerContactField = new JTextField();
        
        JSpinner orderDateSpinner = new JSpinner(new SpinnerDateModel());
        orderDateSpinner.setEditor(new JSpinner.DateEditor(orderDateSpinner, "dd.MM.yyyy"));
        orderDateSpinner.setValue(new Date());
        
        JSpinner deliveryDateSpinner = new JSpinner(new SpinnerDateModel());
        deliveryDateSpinner.setEditor(new JSpinner.DateEditor(deliveryDateSpinner, "dd.MM.yyyy"));
        
        // Установим дату доставки на 7 дней вперед
        Date delivery = new Date();
        delivery.setTime(delivery.getTime() + 7L * 24 * 60 * 60 * 1000);
        deliveryDateSpinner.setValue(delivery);
        
        String[] statusOptions = {"Новый", "Обработан", "Доставляется", "Доставлен"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        
        // Панель для выбора продуктов
        JPanel productsPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        productsPanel.add(new JLabel("Продукт"));
        productsPanel.add(new JLabel("Количество"));
        productsPanel.add(new JLabel("Цена"));
        
        List<JCheckBox> checkBoxes = new ArrayList<>();
        List<JSpinner> quantities = new ArrayList<>();
        List<JLabel> prices = new ArrayList<>();
        Map<String, ObjectId> productMap = new HashMap<>();
        
        for (Document doc : products) {
            String name = doc.getString("name");
            ObjectId id = doc.getObjectId("_id");
            Double price = doc.getDouble("price");
            
            JCheckBox checkBox = new JCheckBox(name);
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
            JLabel priceLabel = new JLabel(price + " руб.");
            
            checkBoxes.add(checkBox);
            quantities.add(quantitySpinner);
            prices.add(priceLabel);
            productMap.put(name, id);
            
            productsPanel.add(checkBox);
            productsPanel.add(quantitySpinner);
            productsPanel.add(priceLabel);
        }
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JPanel customerPanel = new JPanel(new GridLayout(0, 1));
        customerPanel.add(new JLabel("Имя заказчика:"));
        customerPanel.add(customerNameField);
        customerPanel.add(new JLabel("Контакт заказчика:"));
        customerPanel.add(customerContactField);
        customerPanel.add(new JLabel("Дата заказа:"));
        customerPanel.add(orderDateSpinner);
        customerPanel.add(new JLabel("Дата доставки:"));
        customerPanel.add(deliveryDateSpinner);
        customerPanel.add(new JLabel("Статус:"));
        customerPanel.add(statusCombo);
        
        panel.add(customerPanel, BorderLayout.NORTH);
        
        JScrollPane productsScroll = new JScrollPane(productsPanel);
        productsScroll.setPreferredSize(new Dimension(400, 250));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Выберите продукты:"), BorderLayout.NORTH);
        centerPanel.add(productsScroll, BorderLayout.CENTER);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Добавить новый заказ", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String customerName = customerNameField.getText();
                String customerContact = customerContactField.getText();
                Date orderDate = (Date) orderDateSpinner.getValue();
                Date deliveryDate = (Date) deliveryDateSpinner.getValue();
                String status = (String) statusCombo.getSelectedItem();
                
                Document productsDoc = new Document();
                double totalAmount = 0.0;
                
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        String productName = checkBoxes.get(i).getText();
                        int quantity = (Integer) quantities.get(i).getValue();
                        
                        ObjectId productId = productMap.get(productName);
                        Document productDoc = MongoDBConnector.getDocumentById("products", productId);
                        double price = productDoc.getDouble("price");
                        
                        productsDoc.append(productId.toString(), quantity);
                        totalAmount += price * quantity;
                    }
                }
                
                Document orderDoc = new Document()
                    .append("customerName", customerName)
                    .append("customerContact", customerContact)
                    .append("products", productsDoc)
                    .append("orderDate", orderDate)
                    .append("deliveryDate", deliveryDate)
                    .append("status", status)
                    .append("totalAmount", totalAmount);
                
                MongoDBConnector.insertDocument("orders", orderDoc);
                loadData();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка при создании заказа: " + ex.getMessage(), 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteOrder(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String idStr = (String) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, "Вы уверены, что хотите удалить этот заказ?", 
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ObjectId id = new ObjectId(idStr);
                MongoDBConnector.deleteDocument("orders", id);
                loadData();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, выберите заказ для удаления.", 
                "Заказ не выбран", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void searchOrders(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Document filter = new Document("$or", List.of(
            new Document("customerName", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("customerContact", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("status", new Document("$regex", searchTerm).append("$options", "i"))
        ));
        
        List<Document> orders = MongoDBConnector.findDocuments("orders", filter);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : orders) {
            ObjectId id = doc.getObjectId("_id");
            String customerName = doc.getString("customerName");
            String customerContact = doc.getString("customerContact");
            
            Document productsDoc = (Document) doc.get("products");
            StringBuilder productsStr = new StringBuilder();
            if (productsDoc != null) {
                for (String productIdStr : productsDoc.keySet()) {
                    ObjectId productId = new ObjectId(productIdStr);
                    Document productDoc = MongoDBConnector.getDocumentById("products", productId);
                    String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
                    Integer quantity = productsDoc.getInteger(productIdStr);
                    productsStr.append(productName).append(" (").append(quantity).append(" шт.), ");
                }
                if (productsStr.length() > 2) {
                    productsStr.setLength(productsStr.length() - 2);
                }
            }
            
            Date orderDate = doc.getDate("orderDate");
            Date deliveryDate = doc.getDate("deliveryDate");
            String status = doc.getString("status");
            Double totalAmount = doc.getDouble("totalAmount");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                customerName, 
                customerContact, 
                productsStr.toString(), 
                orderDate != null ? dateFormat.format(orderDate) : "", 
                deliveryDate != null ? dateFormat.format(deliveryDate) : "", 
                status,
                totalAmount
            });
        }
    }
    
    private void sortOrders(ActionEvent e) {
        String field = (String) sortField.getSelectedItem();
        int order = sortOrder.getSelectedIndex() == 0 ? 1 : -1; // 1 для возрастания, -1 для убывания
        
        tableModel.setRowCount(0);
        
        List<Document> orders = MongoDBConnector.sortDocuments("orders", field, order);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : orders) {
            ObjectId id = doc.getObjectId("_id");
            String customerName = doc.getString("customerName");
            String customerContact = doc.getString("customerContact");
            
            Document productsDoc = (Document) doc.get("products");
            StringBuilder productsStr = new StringBuilder();
            if (productsDoc != null) {
                for (String productIdStr : productsDoc.keySet()) {
                    ObjectId productId = new ObjectId(productIdStr);
                    Document productDoc = MongoDBConnector.getDocumentById("products", productId);
                    String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
                    Integer quantity = productsDoc.getInteger(productIdStr);
                    productsStr.append(productName).append(" (").append(quantity).append(" шт.), ");
                }
                if (productsStr.length() > 2) {
                    productsStr.setLength(productsStr.length() - 2);
                }
            }
            
            Date orderDate = doc.getDate("orderDate");
            Date deliveryDate = doc.getDate("deliveryDate");
            String status = doc.getString("status");
            Double totalAmount = doc.getDouble("totalAmount");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                customerName, 
                customerContact, 
                productsStr.toString(), 
                orderDate != null ? dateFormat.format(orderDate) : "", 
                deliveryDate != null ? dateFormat.format(deliveryDate) : "", 
                status,
                totalAmount
            });
        }
    }
} 