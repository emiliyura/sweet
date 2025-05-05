package ru.confectionery.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.confectionery.dao.MongoDBConnector;

public class BatchPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, searchButton, sortButton;
    private JTextField searchField;
    private JComboBox<String> sortField;
    private JComboBox<String> sortOrder;
    
    public BatchPanel() {
        setLayout(new BorderLayout());
        
        // Создание модели таблицы
        String[] columns = {"ID", "Продукт", "Количество", "Дата производства", "Срок годности", "Номер партии", "Статус"};
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
        
        sortField = new JComboBox<>(new String[]{"quantity", "productionDate", "expiryDate", "batchNumber", "status"});
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
        addButton.addActionListener(this::addBatch);
        deleteButton.addActionListener(this::deleteBatch);
        searchButton.addActionListener(this::searchBatches);
        sortButton.addActionListener(this::sortBatches);
        
        // Загрузка данных
        loadData();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Document> batches = MongoDBConnector.getAllDocuments("batches");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : batches) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            
            // Получаем имя продукта
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Integer quantity = doc.getInteger("quantity");
            Date productionDate = doc.getDate("productionDate");
            Date expiryDate = doc.getDate("expiryDate");
            String batchNumber = doc.getString("batchNumber");
            String status = doc.getString("status");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                productName, 
                quantity, 
                productionDate != null ? dateFormat.format(productionDate) : "", 
                expiryDate != null ? dateFormat.format(expiryDate) : "", 
                batchNumber, 
                status
            });
        }
    }
    
    private void addBatch(ActionEvent e) {
        // Получаем список продуктов
        List<Document> products = MongoDBConnector.getAllDocuments("products");
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Сначала добавьте продукты!", 
                "Нет продуктов", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Создаем список для выбора продукта
        DefaultComboBoxModel<String> productModel = new DefaultComboBoxModel<>();
        Map<String, ObjectId> productMap = new HashMap<>();
        
        for (Document doc : products) {
            String name = doc.getString("name");
            ObjectId id = doc.getObjectId("_id");
            productModel.addElement(name);
            productMap.put(name, id);
        }
        
        JComboBox<String> productCombo = new JComboBox<>(productModel);
        JTextField quantityField = new JTextField();
        JTextField batchNumberField = new JTextField();
        
        JSpinner productionDateSpinner = new JSpinner(new SpinnerDateModel());
        productionDateSpinner.setEditor(new JSpinner.DateEditor(productionDateSpinner, "dd.MM.yyyy"));
        productionDateSpinner.setValue(new Date());
        
        JSpinner expiryDateSpinner = new JSpinner(new SpinnerDateModel());
        expiryDateSpinner.setEditor(new JSpinner.DateEditor(expiryDateSpinner, "dd.MM.yyyy"));
        
        // Установим срок годности на 30 дней вперед
        Date expiry = new Date();
        expiry.setTime(expiry.getTime() + 30L * 24 * 60 * 60 * 1000);
        expiryDateSpinner.setValue(expiry);
        
        String[] statusOptions = {"Произведено", "На хранении", "Отправлено", "Продано"};
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Продукт:"));
        panel.add(productCombo);
        panel.add(new JLabel("Количество:"));
        panel.add(quantityField);
        panel.add(new JLabel("Дата производства:"));
        panel.add(productionDateSpinner);
        panel.add(new JLabel("Срок годности:"));
        panel.add(expiryDateSpinner);
        panel.add(new JLabel("Номер партии:"));
        panel.add(batchNumberField);
        panel.add(new JLabel("Статус:"));
        panel.add(statusCombo);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Добавить новую партию", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedProduct = (String) productCombo.getSelectedItem();
                ObjectId productId = productMap.get(selectedProduct);
                
                int quantity = Integer.parseInt(quantityField.getText());
                Date productionDate = (Date) productionDateSpinner.getValue();
                Date expiryDate = (Date) expiryDateSpinner.getValue();
                String batchNumber = batchNumberField.getText();
                String status = (String) statusCombo.getSelectedItem();
                
                Document batchDoc = new Document()
                    .append("productId", productId)
                    .append("quantity", quantity)
                    .append("productionDate", productionDate)
                    .append("expiryDate", expiryDate)
                    .append("batchNumber", batchNumber)
                    .append("status", status);
                
                MongoDBConnector.insertDocument("batches", batchDoc);
                loadData();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка формата. Количество должно быть числом.", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteBatch(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String idStr = (String) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, "Вы уверены, что хотите удалить эту партию?", 
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ObjectId id = new ObjectId(idStr);
                MongoDBConnector.deleteDocument("batches", id);
                loadData();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, выберите партию для удаления.", 
                "Партия не выбрана", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void searchBatches(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Document filter = new Document("$or", List.of(
            new Document("batchNumber", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("status", new Document("$regex", searchTerm).append("$options", "i"))
        ));
        
        List<Document> batches = MongoDBConnector.findDocuments("batches", filter);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : batches) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            
            // Получаем имя продукта
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Integer quantity = doc.getInteger("quantity");
            Date productionDate = doc.getDate("productionDate");
            Date expiryDate = doc.getDate("expiryDate");
            String batchNumber = doc.getString("batchNumber");
            String status = doc.getString("status");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                productName, 
                quantity, 
                productionDate != null ? dateFormat.format(productionDate) : "", 
                expiryDate != null ? dateFormat.format(expiryDate) : "", 
                batchNumber, 
                status
            });
        }
    }
    
    private void sortBatches(ActionEvent e) {
        String field = (String) sortField.getSelectedItem();
        int order = sortOrder.getSelectedIndex() == 0 ? 1 : -1; // 1 для возрастания, -1 для убывания
        
        tableModel.setRowCount(0);
        
        List<Document> batches = MongoDBConnector.sortDocuments("batches", field, order);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        
        for (Document doc : batches) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            
            // Получаем имя продукта
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Integer quantity = doc.getInteger("quantity");
            Date productionDate = doc.getDate("productionDate");
            Date expiryDate = doc.getDate("expiryDate");
            String batchNumber = doc.getString("batchNumber");
            String status = doc.getString("status");
            
            tableModel.addRow(new Object[]{
                id.toString(), 
                productName, 
                quantity, 
                productionDate != null ? dateFormat.format(productionDate) : "", 
                expiryDate != null ? dateFormat.format(expiryDate) : "", 
                batchNumber, 
                status
            });
        }
    }
} 