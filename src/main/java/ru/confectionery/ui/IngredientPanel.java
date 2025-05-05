package ru.confectionery.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.confectionery.dao.MongoDBConnector;

public class IngredientPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, searchButton, sortButton;
    private JTextField searchField;
    private JComboBox<String> sortField;
    private JComboBox<String> sortOrder;
    
    public IngredientPanel() {
        setLayout(new BorderLayout());
        
        // Создание модели таблицы
        String[] columns = {"ID", "Название", "Единица измерения", "Цена за единицу", "Количество на складе"};
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
        
        sortField = new JComboBox<>(new String[]{"name", "unit", "pricePerUnit", "stockQuantity"});
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
        addButton.addActionListener(this::addIngredient);
        deleteButton.addActionListener(this::deleteIngredient);
        searchButton.addActionListener(this::searchIngredients);
        sortButton.addActionListener(this::sortIngredients);
        
        // Загрузка данных
        loadData();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Document> ingredients = MongoDBConnector.getAllDocuments("ingredients");
        
        for (Document doc : ingredients) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String unit = doc.getString("unit");
            Double pricePerUnit = doc.getDouble("pricePerUnit");
            Integer stockQuantity = doc.getInteger("stockQuantity");
            
            tableModel.addRow(new Object[]{id.toString(), name, unit, pricePerUnit, stockQuantity});
        }
    }
    
    private void addIngredient(ActionEvent e) {
        JTextField nameField = new JTextField();
        JTextField unitField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Единица измерения:"));
        panel.add(unitField);
        panel.add(new JLabel("Цена за единицу:"));
        panel.add(priceField);
        panel.add(new JLabel("Количество на складе:"));
        panel.add(stockField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Добавить новый ингредиент", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String unit = unitField.getText();
                double pricePerUnit = Double.parseDouble(priceField.getText());
                int stockQuantity = Integer.parseInt(stockField.getText());
                
                Document ingredientDoc = new Document()
                    .append("name", name)
                    .append("unit", unit)
                    .append("pricePerUnit", pricePerUnit)
                    .append("stockQuantity", stockQuantity);
                
                MongoDBConnector.insertDocument("ingredients", ingredientDoc);
                loadData();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка формата. Цена и количество должны быть числами.", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteIngredient(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String idStr = (String) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, "Вы уверены, что хотите удалить этот ингредиент?", 
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ObjectId id = new ObjectId(idStr);
                MongoDBConnector.deleteDocument("ingredients", id);
                loadData();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, выберите ингредиент для удаления.", 
                "Ингредиент не выбран", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void searchIngredients(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Document filter = new Document("$or", List.of(
            new Document("name", new Document("$regex", searchTerm).append("$options", "i")),
            new Document("unit", new Document("$regex", searchTerm).append("$options", "i"))
        ));
        
        List<Document> ingredients = MongoDBConnector.findDocuments("ingredients", filter);
        
        for (Document doc : ingredients) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String unit = doc.getString("unit");
            Double pricePerUnit = doc.getDouble("pricePerUnit");
            Integer stockQuantity = doc.getInteger("stockQuantity");
            
            tableModel.addRow(new Object[]{id.toString(), name, unit, pricePerUnit, stockQuantity});
        }
    }
    
    private void sortIngredients(ActionEvent e) {
        String field = (String) sortField.getSelectedItem();
        int order = sortOrder.getSelectedIndex() == 0 ? 1 : -1; // 1 для возрастания, -1 для убывания
        
        tableModel.setRowCount(0);
        
        List<Document> ingredients = MongoDBConnector.sortDocuments("ingredients", field, order);
        
        for (Document doc : ingredients) {
            ObjectId id = doc.getObjectId("_id");
            String name = doc.getString("name");
            String unit = doc.getString("unit");
            Double pricePerUnit = doc.getDouble("pricePerUnit");
            Integer stockQuantity = doc.getInteger("stockQuantity");
            
            tableModel.addRow(new Object[]{id.toString(), name, unit, pricePerUnit, stockQuantity});
        }
    }
} 