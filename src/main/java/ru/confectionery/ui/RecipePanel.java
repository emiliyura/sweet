package ru.confectionery.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.bson.Document;
import org.bson.types.ObjectId;
import ru.confectionery.dao.MongoDBConnector;

public class RecipePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, searchButton, sortButton;
    private JTextField searchField;
    private JComboBox<String> sortOrder;
    
    public RecipePanel() {
        setLayout(new BorderLayout());
        
        // Создание модели таблицы
        String[] columns = {"ID", "Продукт", "Ингредиенты", "Инструкции"};
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
        
        sortOrder = new JComboBox<>(new String[]{"По возрастанию", "По убыванию"});
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(new JLabel("Поиск по инструкции:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(new JLabel("Сортировать:"));
        buttonPanel.add(sortOrder);
        buttonPanel.add(sortButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Обработчики событий
        addButton.addActionListener(this::addRecipe);
        deleteButton.addActionListener(this::deleteRecipe);
        searchButton.addActionListener(this::searchRecipes);
        sortButton.addActionListener(this::sortRecipes);
        
        // Загрузка данных
        loadData();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        List<Document> recipes = MongoDBConnector.getAllDocuments("recipes");
        
        for (Document doc : recipes) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Document ingredientsDoc = (Document) doc.get("ingredients");
            StringBuilder ingredientsStr = new StringBuilder();
            if (ingredientsDoc != null) {
                for (String key : ingredientsDoc.keySet()) {
                    ingredientsStr.append(key).append(": ").append(ingredientsDoc.getDouble(key)).append(", ");
                }
                if (ingredientsStr.length() > 2) {
                    ingredientsStr.setLength(ingredientsStr.length() - 2);
                }
            }
            
            String instructions = doc.getString("instructions");
            
            tableModel.addRow(new Object[]{id.toString(), productName, ingredientsStr.toString(), instructions});
        }
    }
    
    private void addRecipe(ActionEvent e) {
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
        
        // Получаем список ингредиентов
        List<Document> ingredients = MongoDBConnector.getAllDocuments("ingredients");
        
        // Панель для ингредиентов
        JPanel ingredientsPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        ingredientsPanel.add(new JLabel("Ингредиент"));
        ingredientsPanel.add(new JLabel("Количество"));
        ingredientsPanel.add(new JLabel("Единица"));
        
        List<JCheckBox> checkBoxes = new ArrayList<>();
        List<JTextField> quantities = new ArrayList<>();
        List<JLabel> units = new ArrayList<>();
        
        for (Document doc : ingredients) {
            String name = doc.getString("name");
            String unit = doc.getString("unit");
            
            JCheckBox checkBox = new JCheckBox(name);
            JTextField quantityField = new JTextField("0");
            JLabel unitLabel = new JLabel(unit);
            
            checkBoxes.add(checkBox);
            quantities.add(quantityField);
            units.add(unitLabel);
            
            ingredientsPanel.add(checkBox);
            ingredientsPanel.add(quantityField);
            ingredientsPanel.add(unitLabel);
        }
        
        JTextArea instructionsArea = new JTextArea(5, 20);
        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Выберите продукт:"), BorderLayout.NORTH);
        panel.add(productCombo, BorderLayout.NORTH);
        
        JScrollPane ingredientsScroll = new JScrollPane(ingredientsPanel);
        ingredientsScroll.setPreferredSize(new Dimension(400, 200));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Выберите ингредиенты:"), BorderLayout.NORTH);
        centerPanel.add(ingredientsScroll, BorderLayout.CENTER);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JLabel("Инструкции по приготовлению:"), BorderLayout.NORTH);
        southPanel.add(instructionsScroll, BorderLayout.CENTER);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Добавить новый рецепт", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String selectedProduct = (String) productCombo.getSelectedItem();
                ObjectId productId = productMap.get(selectedProduct);
                
                Document ingredientsDoc = new Document();
                
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        String ingredientName = checkBoxes.get(i).getText();
                        double quantity = Double.parseDouble(quantities.get(i).getText());
                        ingredientsDoc.append(ingredientName, quantity);
                    }
                }
                
                String instructions = instructionsArea.getText();
                
                Document recipeDoc = new Document()
                    .append("productId", productId)
                    .append("ingredients", ingredientsDoc)
                    .append("instructions", instructions);
                
                MongoDBConnector.insertDocument("recipes", recipeDoc);
                loadData();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Ошибка формата. Количество должно быть числом.", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteRecipe(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String idStr = (String) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this, "Вы уверены, что хотите удалить этот рецепт?", 
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ObjectId id = new ObjectId(idStr);
                MongoDBConnector.deleteDocument("recipes", id);
                loadData();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Пожалуйста, выберите рецепт для удаления.", 
                "Рецепт не выбран", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void searchRecipes(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Document filter = new Document("instructions", 
            new Document("$regex", searchTerm).append("$options", "i"));
        
        List<Document> recipes = MongoDBConnector.findDocuments("recipes", filter);
        
        for (Document doc : recipes) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Document ingredientsDoc = (Document) doc.get("ingredients");
            StringBuilder ingredientsStr = new StringBuilder();
            if (ingredientsDoc != null) {
                for (String key : ingredientsDoc.keySet()) {
                    ingredientsStr.append(key).append(": ").append(ingredientsDoc.getDouble(key)).append(", ");
                }
                if (ingredientsStr.length() > 2) {
                    ingredientsStr.setLength(ingredientsStr.length() - 2);
                }
            }
            
            String instructions = doc.getString("instructions");
            
            tableModel.addRow(new Object[]{id.toString(), productName, ingredientsStr.toString(), instructions});
        }
    }
    
    private void sortRecipes(ActionEvent e) {
        int order = sortOrder.getSelectedIndex() == 0 ? 1 : -1; // 1 для возрастания, -1 для убывания
        
        tableModel.setRowCount(0);
        
        List<Document> recipes = MongoDBConnector.sortDocuments("recipes", "instructions", order);
        
        for (Document doc : recipes) {
            ObjectId id = doc.getObjectId("_id");
            ObjectId productId = doc.getObjectId("productId");
            Document productDoc = MongoDBConnector.getDocumentById("products", productId);
            String productName = productDoc != null ? productDoc.getString("name") : "Неизвестный продукт";
            
            Document ingredientsDoc = (Document) doc.get("ingredients");
            StringBuilder ingredientsStr = new StringBuilder();
            if (ingredientsDoc != null) {
                for (String key : ingredientsDoc.keySet()) {
                    ingredientsStr.append(key).append(": ").append(ingredientsDoc.getDouble(key)).append(", ");
                }
                if (ingredientsStr.length() > 2) {
                    ingredientsStr.setLength(ingredientsStr.length() - 2);
                }
            }
            
            String instructions = doc.getString("instructions");
            
            tableModel.addRow(new Object[]{id.toString(), productName, ingredientsStr.toString(), instructions});
        }
    }
} 