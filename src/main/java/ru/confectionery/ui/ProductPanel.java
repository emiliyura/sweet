package ru.confectionery.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.confectionery.dao.MongoDBConnector;
import ru.confectionery.model.Product;

public class ProductPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, searchButton, sortButton;
    private JTextField searchField;
    private JComboBox<String> sortField;
    private JComboBox<String> sortOrder;
    
    public ProductPanel() {
        setLayout(new BorderLayout());
        
        // Создание модели таблицы
        String[] columns = {"ID", "Название", "Тип", "Цена", "Вес (г)", "Описание"};
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
        
        sortField = new JComboBox<>(new String[]{"name", "type", "price", "weight"});
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
        addButton.addActionListener(this::addProduct);
        deleteButton.addActionListener(this::deleteProduct);
        searchButton.addActionListener(this::searchProducts);
        sortButton.addActionListener(this::sortProducts);
        
        // Загрузка данных
        loadData();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Document> products = MongoDBConnector.getAllDocuments("products");
        
        for (Document doc : products) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String type = doc.getString("type");
            Double price = doc.getDouble("price");
            Integer weight = doc.getInteger("weight");
            String description = doc.getString("description");
            
            tableModel.addRow(new Object[]{id.toString(), name, type, price, weight, description});
        }
    }
    
    private void addProduct(ActionEvent e) {
        JTextField nameField = new JTextField();
        JTextField typeField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField weightField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Тип:"));
        panel.add(typeField);
        panel.add(new JLabel("Цена:"));
        panel.add(priceField);
        panel.add(new JLabel("Вес (г):"));
        panel.add(weightField);
        panel.add(new JLabel("Описание:"));
        panel.add(new JScrollPane(descriptionArea));
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Добавить новый продукт", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String type = typeField.getText();
                double price = Double.parseDouble(priceField.getText());
                int weight = Integer.parseInt(weightField.getText());
                String description = descriptionArea.getText();
                
                Document productDoc = new Document()
                    .append("name", name)
                    .append("type", type)
                    .append("price", price)
                    .append("weight", weight)
                    .append("description", description);
                
                MongoDBConnector.insertDocument("products", productDoc);
                loadData();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка формата. Цена и вес должны быть числами.", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteProduct(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String idStr = (String) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, "Вы уверены, что хотите удалить этот продукт?", 
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ObjectId id = new ObjectId(idStr);
                MongoDBConnector.deleteDocument("products", id);
                loadData();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, выберите продукт для удаления.", 
                "Продукт не выбран", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void searchProducts(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Document filter = new Document("$or", List.of(
            new Document("name", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("type", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("description", new Document("$regex", searchTerm).append("$options", "i"))
        ));
        
        List<Document> products = MongoDBConnector.findDocuments("products", filter);
        
        for (Document doc : products) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String type = doc.getString("type");
            Double price = doc.getDouble("price");
            Integer weight = doc.getInteger("weight");
            String description = doc.getString("description");
            
            tableModel.addRow(new Object[]{id.toString(), name, type, price, weight, description});
        }
    }
    
    private void sortProducts(ActionEvent e) {
        String field = (String) sortField.getSelectedItem();
        int order = sortOrder.getSelectedIndex() == 0 ? 1 : -1; // 1 для возрастания, -1 для убывания
        
        tableModel.setRowCount(0);
        
        List<Document> products = MongoDBConnector.sortDocuments("products", field, order);
        
        for (Document doc : products) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String type = doc.getString("type");
            Double price = doc.getDouble("price");
            Integer weight = doc.getInteger("weight");
            String description = doc.getString("description");
            
            tableModel.addRow(new Object[]{id.toString(), name, type, price, weight, description});
        }
    }
} 