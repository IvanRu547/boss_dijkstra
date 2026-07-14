package ui;

import model.Edge;
import model.Graph;
import model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

import algorithm.BellmanFordAlgorithm;
import algorithm.BellmanFordResult;
import algorithm.BellmanFordStep;
import java.util.List;

public class MainWindow extends JFrame {

    private Graph graph;
    private GraphPanel canvas;

    private Timer autoTimer;


    private JButton addVertexBtn;
    private JButton addEdgeBtn;
    private JButton runBtn;
    private JButton stepBtn;
    private JButton resetBtn;
    private JButton clearBtn;
    private JTextArea infoArea;
    private JButton startBtn;
    private JButton finishBtn;
    private JButton autoBtn;
    private JButton arrangeBtn;

    private JTextField speedField;
    private int autoSpeed = 500; // значение по умолчанию в мс

    private String currentMode = "auto";
    private int vertexCounter = 0;
    private Vertex selectedVertex = null;
    private BellmanFordAlgorithm algorithm;
    private BellmanFordResult result;
    private int currentStepIndex;
    private Vertex startVertex;
    private Vertex endVertex;

    private static final Color ACTIVE_COLOR = new Color(0xC8, 0xD8, 0xE8);
    private static final Color HOVER_COLOR = new Color(0xE0, 0xE0, 0xE0);
    private static final Color ACTIVE_HOVER_COLOR = new Color(0xB0, 0xC8, 0xE0);
    private static final Color PRESSED_COLOR = Color.LIGHT_GRAY;

    public MainWindow() {
        graph = new Graph();
        algorithm = new BellmanFordAlgorithm();
        currentStepIndex = 0;

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
                } else if (currentMode.equals("addEdge")) {
                    handleEdgeCreation(clicked);
                } else if (currentMode.equals("selectStart")) {
                    if (clicked != null) {
                        startVertex = clicked;
                        canvas.setStartVertex(startVertex);
                        canvas.repaint();
                        runBtn.setEnabled(true);
                        infoArea.setText("Стартовая вершина: " + startVertex.getName() + ". Выберите финиш.");
                        currentMode = "selectEnd";
                    } else {
                        infoArea.setText("Кликните по вершине, чтобы выбрать старт.");
                    }
                } else if (currentMode.equals("selectEnd")) {
                    if (clicked != null && !clicked.equals(startVertex)) {
                        endVertex = clicked;
                        canvas.setEndVertex(endVertex);
                        canvas.repaint();
                        infoArea.setText("Конечная вершина: " + endVertex.getName() + ". Нажмите 'Запустить'.");
                        currentMode = "auto";
                    } else if (clicked != null && clicked.equals(startVertex)) {
                        infoArea.setText("Стартовая и конечная вершины не должны совпадать.");
                    } else {
                        infoArea.setText("Кликните по вершине, чтобы выбрать финиш.");
                    }
                } else if (currentMode.equals("auto")) {
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
        buttonPanel.setLayout(new GridLayout(12, 1, 5, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addVertexBtn = new JButton("Вершина");
        addEdgeBtn = new JButton("Ребро");
        runBtn = new JButton("Запустить");
        stepBtn = new JButton("Шаг вперёд");
        resetBtn = new JButton("Сброс");
        clearBtn = new JButton("Очистить");
        startBtn = new JButton("Старт");
        finishBtn = new JButton("Финиш");
        autoBtn = new JButton("Авто");
        arrangeBtn = new JButton("Расставить");
        arrangeBtn.setEnabled(false);

        startBtn.addActionListener(e -> {
            currentMode = "selectStart";
            deselectVertex();
            infoArea.setText("Выберите стартовую вершину.");
        });

        finishBtn.addActionListener(e -> {
            currentMode = "selectEnd";
            deselectVertex();
            infoArea.setText("Выберите конечную вершину.");
        });

        for (JButton btn : new JButton[]{addVertexBtn, addEdgeBtn, clearBtn}) {
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            addHoverEffect(btn);
        }

        addVertexBtn.setEnabled(true);
        addEdgeBtn.setEnabled(true);
        runBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        clearBtn.setEnabled(true);
        autoBtn.setEnabled(false);

        addVertexBtn.addActionListener(e -> setMode("addVertex"));
        addEdgeBtn.addActionListener(e -> setMode("addEdge"));
        clearBtn.addActionListener(e -> clearGraph());
        runBtn.addActionListener(e -> runAlgorithm());
        stepBtn.addActionListener(e -> showNextStep());
        resetBtn.addActionListener(e -> resetAlgorithm());
        autoBtn.addActionListener(e -> startAutoPlay());


        buttonPanel.add(addVertexBtn);
        buttonPanel.add(addEdgeBtn);
        buttonPanel.add(startBtn);
        buttonPanel.add(finishBtn);
        buttonPanel.add(runBtn);
        buttonPanel.add(stepBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(arrangeBtn);
        buttonPanel.add(clearBtn);


        // Панель для авто-режима: кнопка + поле скорости
        JPanel autoPanel = new JPanel(new BorderLayout(5, 0));
        autoPanel.add(autoBtn, BorderLayout.CENTER);

        speedField = new JTextField("500", 4);
        speedField.setToolTipText("Скорость авто-режима в мс (100-2000)");
        JLabel speedLabel = new JLabel("мс");
        JPanel speedPanel = new JPanel(new BorderLayout(2, 0));
        speedPanel.add(speedField, BorderLayout.CENTER);
        speedPanel.add(speedLabel, BorderLayout.EAST);
        autoPanel.add(speedPanel, BorderLayout.EAST);

        buttonPanel.add(autoPanel);


        add(buttonPanel, BorderLayout.EAST);

        infoArea = new JTextArea(3, 40);
        infoArea.setText("Авто-режим: клик по пустому месту — вершина, клик по вершине — ребро.");
        infoArea.setEditable(false);
        infoArea.setBorder(BorderFactory.createTitledBorder("Информация"));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(900, 80));
        add(scrollPane, BorderLayout.SOUTH);

        updateButtonStyles();

        setLocationRelativeTo(null);
    }

    private void addHoverEffect(JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setContentAreaFilled(true);
                    if (isActiveButton(btn)) {
                        btn.setBackground(ACTIVE_HOVER_COLOR);
                    } else {
                        btn.setBackground(HOVER_COLOR);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    applyModeStyle(btn);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(PRESSED_COLOR);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.isEnabled()) {
                    if (btn.contains(e.getPoint())) {
                        if (isActiveButton(btn)) {
                            btn.setBackground(ACTIVE_HOVER_COLOR);
                        } else {
                            btn.setBackground(HOVER_COLOR);
                        }
                    } else {
                        applyModeStyle(btn);
                    }
                }
            }
        });
    }

    private boolean isActiveButton(JButton btn) {
        return (btn == addVertexBtn && currentMode.equals("addVertex")) ||
               (btn == addEdgeBtn && currentMode.equals("addEdge"));
    }

    private void applyModeStyle(JButton btn) {
        if (!btn.isEnabled()) return;

        if (btn == addVertexBtn) {
            if (currentMode.equals("addVertex")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            } else {
                btn.setBackground(null);
                btn.setContentAreaFilled(false);
            }
        } else if (btn == addEdgeBtn) {
            if (currentMode.equals("addEdge")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            } else {
                btn.setBackground(null);
                btn.setContentAreaFilled(false);
            }
        } else if (btn == clearBtn) {
            btn.setBackground(null);
            btn.setContentAreaFilled(false);
        }
    }

    private void updateButtonStyles() {
        applyModeStyle(addVertexBtn);
        applyModeStyle(addEdgeBtn);
        applyModeStyle(clearBtn);
    }

    private void setMode(String mode) {
        if (currentMode.equals(mode)) {
            currentMode = "auto";
            infoArea.setText("Авто-режим: клик по пустому месту — вершина, клик по вершине — ребро.");
        } else {
            currentMode = mode;
            if (mode.equals("addVertex")) {
                infoArea.setText("Режим: только добавление вершин. Кликните по пустому месту.");
            } else if (mode.equals("addEdge")) {
                infoArea.setText("Режим: только добавление рёбер. Кликните по вершине.");
            }
        }
        updateButtonStyles();
        deselectVertex();

        checkMouseHover(addVertexBtn);
        checkMouseHover(addEdgeBtn);
        checkMouseHover(clearBtn);
    }

    private void checkMouseHover(JButton btn) {
        if (!btn.isEnabled()) return;
        // getMousePosition() может вернуть null, если компонент не отображается
        Point mousePos = btn.getMousePosition();
        if (mousePos != null && btn.contains(mousePos)) {
            btn.setContentAreaFilled(true);
            if (isActiveButton(btn)) {
                btn.setBackground(ACTIVE_HOVER_COLOR);
            } else {
                btn.setBackground(HOVER_COLOR);
            }
        }
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
        String defaultWeight = (existing != null) ? String.valueOf(existing.getWeight()) : "";

        String input = JOptionPane.showInputDialog(
                MainWindow.this,
                "Введите вес ребра (" + from.getName() + " -> " + to.getName() + "):",
                defaultWeight
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                int weight = Integer.parseInt(input.trim());
                graph.addEdge(from, to, weight);
                deselectVertex();
                infoArea.setText("Добавлено ребро " + from.getName() + " -> " + to.getName() + " с весом " + weight);
                canvas.repaint();
            } catch (NumberFormatException ex) {
                infoArea.setText("Ошибка: вес должен быть целым числом.");
            }
        } else {
            deselectVertex();
            infoArea.setText("Добавление ребра отменено.");
        }
    }

    private void clearGraph() {
        graph.clear();
        vertexCounter = 0;
        deselectVertex();
        resetAlgorithm();
        infoArea.setText("Граф очищен.");
        canvas.repaint();
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
    private void runAlgorithm() {
        if (startVertex == null) {
            infoArea.setText("Сначала выберите стартовую вершину.");
            return;
        }

        result = algorithm.execute(graph, startVertex);
        currentStepIndex = 0;

        if (result.hasNegativeCycle()) {
            infoArea.setText("Обнаружен отрицательный цикл! Некоторые вершины недостижимы.");
        } else {
            infoArea.setText("Алгоритм запущен. Всего шагов: " + result.getStepsHistory().size() + ". Нажмите 'Шаг вперёд'.");
        }

        stepBtn.setEnabled(true);
        resetBtn.setEnabled(true);
        autoBtn.setEnabled(true);
        runBtn.setEnabled(false);
    }

    private void showNextStep() {
        if (result == null) return;

        if (currentStepIndex < result.getStepsHistory().size()) {
            BellmanFordStep step = result.getStepsHistory().get(currentStepIndex);
            canvas.setCurrentStep(step);   // <-- ДОБАВИТЬ
            canvas.repaint();              // <-- ДОБАВИТЬ
            infoArea.setText(step.getStepDescription());
            currentStepIndex++;
        }

        if (currentStepIndex >= result.getStepsHistory().size()) {
            showFinalPath();
            stepBtn.setEnabled(false);
            autoBtn.setEnabled(false);
        }
    }

    private void resetAlgorithm() {
        if (autoTimer != null) autoTimer.stop();
        autoBtn.setEnabled(false);
        result = null;
        currentStepIndex = 0;
        startVertex = null;
        endVertex = null;
        canvas.setStartVertex(null);
        canvas.setEndVertex(null);
        canvas.clearHighlights();
        canvas.repaint();
        infoArea.setText("Сброшено. Выберите стартовую и конечную вершины.");
        stepBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        runBtn.setEnabled(false);
    }
    private void startAutoPlay() {
        if (result == null) return;

        // Читаем скорость из поля
        try {
            autoSpeed = Integer.parseInt(speedField.getText().trim());
            if (autoSpeed < 100) autoSpeed = 100;
            if (autoSpeed > 2000) autoSpeed = 2000;
        } catch (NumberFormatException ex) {
            autoSpeed = 500;
            speedField.setText("500");
        }

        if (autoTimer != null) autoTimer.stop();

        autoTimer = new Timer(autoSpeed, e -> {
            if (currentStepIndex < result.getStepsHistory().size()) {
                BellmanFordStep step = result.getStepsHistory().get(currentStepIndex);
                canvas.setCurrentStep(step);   // <-- ДОБАВИТЬ
                canvas.repaint();              // <-- ДОБАВИТЬ
                infoArea.setText(step.getStepDescription());
                currentStepIndex++;
            } else {
                autoTimer.stop();
                showFinalPath();
            }
        });
        autoTimer.start();
    }

    private void showFinalPath() {
        if (result == null) return;

        if (endVertex != null) {
            int dist = result.getFinalDistances().get(endVertex);

            if (result.hasNegativeCycle() && result.getUnreachableVertices().contains(endVertex)) {
                infoArea.setText("Вершина " + endVertex.getName() + " находится в отрицательном цикле. Путь не существует.");
            } else if (dist == 1_000_000_000) {
                infoArea.setText("Путь до вершины " + endVertex.getName() + " не найден.");
            } else {
                List<Vertex> path = algorithm.getShortestPath(endVertex, result.getPredecessors(), result.getFinalDistances());
                if (path.isEmpty()) {
                    infoArea.setText("Путь до вершины " + endVertex.getName() + " не найден.");
                } else {
                    StringBuilder pathStr = new StringBuilder();
                    for (int i = 0; i < path.size(); i++) {
                        if (i > 0) pathStr.append(" -> ");
                        pathStr.append(path.get(i).getName());
                    }
                    infoArea.setText("Кратчайший путь до " + endVertex.getName() + ": " + pathStr + ". Длина: " + dist + ".");
                }
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Кратчайшие пути от вершины ").append(startVertex.getName()).append(" ===\n");

            if (result.hasNegativeCycle()) {
                sb.append("⚠ Обнаружен отрицательный цикл!\n");
            }

            for (Vertex v : graph.getVertices()) {
                if (result.hasNegativeCycle() && result.getUnreachableVertices().contains(v)) {
                    sb.append("До ").append(v.getName()).append(": в отрицательном цикле\n");
                } else {
                    int dist = result.getFinalDistances().get(v);
                    if (dist == 1_000_000_000) {
                        sb.append("До ").append(v.getName()).append(": путь не найден\n");
                    } else {
                        List<Vertex> path = algorithm.getShortestPath(v, result.getPredecessors(), result.getFinalDistances());
                        if (path.isEmpty()) {
                            sb.append("До ").append(v.getName()).append(": путь не найден\n");
                        } else {
                            StringBuilder pathStr = new StringBuilder();
                            for (int i = 0; i < path.size(); i++) {
                                if (i > 0) pathStr.append(" -> ");
                                pathStr.append(path.get(i).getName());
                            }
                            sb.append("До ").append(v.getName()).append(": ").append(pathStr).append(" (длина ").append(dist).append(")\n");
                        }
                    }
                }
            }
            infoArea.setText(sb.toString());
        }
    }
}