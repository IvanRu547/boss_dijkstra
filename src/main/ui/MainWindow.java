package main.ui;

import main.model.Edge;
import main.model.Graph;
import main.model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

public class MainWindow extends JFrame {

    private Graph graph;
    private GraphPanel canvas;

    private JButton addVertexBtn;
    private JButton addEdgeBtn;
    private JButton runBtn;
    private JButton stepBtn;
    private JButton resetBtn;
    private JButton clearBtn;
    private JTextArea infoArea;

    private String currentMode = "auto";
    private int vertexCounter = 0;
    private Vertex selectedVertex = null;

    public MainWindow() {
        graph = new Graph();

        setTitle("Визуализатор алгоритма Форда-Беллмана");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        canvas = new GraphPanel(graph);
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(BorderFactory.createEmptyBorder());

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point graphPoint = canvas.screenToGraph(e.getX(), e.getY());
                int x = graphPoint.x;
                int y = graphPoint.y;
                Vertex clicked = findVertexAt(x, y);

                if (currentMode.equals("addVertex")) {
                    if (clicked == null) {
                        createVertex(x, y);
                    } else {
                        infoArea.setText("Здесь уже есть вершина. Выберите пустое место.");
                    }
                }
                else if (currentMode.equals("addEdge")) {
                    handleEdgeCreation(clicked);
                }
                else if (currentMode.equals("auto")) {
                    if (clicked == null) {
                        createVertex(x, y);
                    } else {
                        handleEdgeCreation(clicked);
                    }
                }
            }
        });

        add(canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(8, 1, 5, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addVertexBtn = new JButton("Вершина");
        addEdgeBtn = new JButton("Ребро");
        runBtn = new JButton("Запустить");
        stepBtn = new JButton("Шаг вперёд");
        resetBtn = new JButton("Сброс");
        clearBtn = new JButton("Очистить");

        addVertexBtn.setEnabled(true);
        addEdgeBtn.setEnabled(true);
        runBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        clearBtn.setEnabled(true);

        addVertexBtn.addActionListener(e -> switchMode("addVertex", "Режим: только добавление вершин. Кликните по пустому месту."));
        addEdgeBtn.addActionListener(e -> switchMode("addEdge", "Режим: только добавление рёбер. Кликните по вершине."));
        clearBtn.addActionListener(e -> clearGraph());

        buttonPanel.add(addVertexBtn);
        buttonPanel.add(addEdgeBtn);
        buttonPanel.add(runBtn);
        buttonPanel.add(stepBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(clearBtn);

        add(buttonPanel, BorderLayout.EAST);

        infoArea = new JTextArea(3, 40);
        infoArea.setText("Авто-режим: клик по пустому месту — вершина, клик по вершине — ребро.");
        infoArea.setEditable(false);
        infoArea.setBorder(BorderFactory.createTitledBorder("Информация"));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(900, 80));
        add(scrollPane, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void createVertex(int x, int y) {
        Vertex v = new Vertex(vertexCounter, String.valueOf(vertexCounter), x, y);
        vertexCounter++;
        graph.addVertex(v);
        canvas.repaint();
        infoArea.setText("Добавлена вершина #" + v.getId());
    }

    private void handleEdgeCreation(Vertex clicked) {
        if (clicked == null) {
            deselectVertex();
            infoArea.setText("Кликните по вершине.");
            return;
        }

        if (selectedVertex == null) {
            selectVertex(clicked);
        } else if (clicked.equals(selectedVertex)) {
            deselectVertex();
            infoArea.setText("Выбор отменён.");
        } else {
            createEdgeBetween(selectedVertex, clicked);
        }
    }

    private void selectVertex(Vertex v) {
        selectedVertex = v;
        canvas.setSelectedVertex(v);
        canvas.repaint();
        infoArea.setText("Выбрана вершина " + v.getName() + ". Выберите вторую вершину.");
    }

    private void deselectVertex() {
        selectedVertex = null;
        canvas.setSelectedVertex(null);
        canvas.repaint();
    }

    private void createEdgeBetween(Vertex from, Vertex to) {
        Edge existing = graph.getEdgeBetween(from, to);
        Edge reverse = graph.getEdgeBetween(to, from);

        // Если есть обратное ребро и нет прямого — создаём встречное без диалога
        if (reverse != null && existing == null) {
            graph.addEdge(from, to, reverse.getWeight());
            deselectVertex();
            infoArea.setText("Добавлено встречное ребро " + from.getName() + " -> " + to.getName() + " с весом " + reverse.getWeight());
            return;
        }

        // Иначе показываем диалог
        String defaultWeight = "";
        if (existing != null) {
            defaultWeight = String.valueOf(existing.getWeight());
        } else if (reverse != null) {
            defaultWeight = String.valueOf(reverse.getWeight());
        }

        String input = JOptionPane.showInputDialog(
                MainWindow.this,
                "Введите вес ребра (" + from.getName() + " -> " + to.getName() + "):",
                defaultWeight
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                int weight = Integer.parseInt(input.trim());
                graph.addEdge(from, to, weight);

                // Синхронизируем обратное ребро
                Edge reverseAfter = graph.getEdgeBetween(to, from);
                if (reverseAfter != null) {
                    reverseAfter.setWeight(weight);
                }

                deselectVertex();
                infoArea.setText("Добавлено ребро " + from.getName() + " -> " + to.getName() + " с весом " + weight);
            } catch (NumberFormatException ex) {
                infoArea.setText("Ошибка: вес должен быть целым числом.");
            }
        }
    }

    private void switchMode(String mode, String message) {
        currentMode = mode;
        deselectVertex();
        infoArea.setText(message);
    }

    private void clearGraph() {
        graph.clear();
        vertexCounter = 0;
        deselectVertex();
        infoArea.setText("Граф очищен.");
    }

    private Vertex findVertexAt(int x, int y) {
        for (Vertex v : graph.getVertices()) {
            int vx = (int) v.getX();
            int vy = (int) v.getY();
            double dist = Math.sqrt((x - vx) * (x - vx) + (y - vy) * (y - vy));
            if (dist <= 25) {
                return v;
            }
        }
        return null;
    }
}