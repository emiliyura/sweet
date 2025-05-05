package ru.confectionery.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ru.confectionery.dao.MongoDBConnector;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        setTitle("Система управления производством сладостей");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Подключение к MongoDB при запуске - вызываем ДО создания панелей
        MongoDBConnector.connect();
        
        tabbedPane = new JTabbedPane();
        
        // Добавление вкладок
        tabbedPane.addTab("Продукты", new ProductPanel());
        tabbedPane.addTab("Ингредиенты", new IngredientPanel());
        tabbedPane.addTab("Рецепты", new RecipePanel());
        tabbedPane.addTab("Партии", new BatchPanel());
        tabbedPane.addTab("Заказы", new OrderPanel());
        
        add(tabbedPane);
        
        // Отключение при закрытии
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MongoDBConnector.close();
                super.windowClosing(e);
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
} 