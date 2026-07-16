package ui;

import model.Edge;
import model.Graph;
import model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import algorithm.BellmanFordAlgorithm;
import algorithm.BellmanFordResult;
import algorithm.BellmanFordStep;
import java.util.List;
import java.util.Map;

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
    private JButton startBtn;
    private JButton finishBtn;
    private JButton autoBtn;
    private JButton arrangeBtn;
    private JButton saveLogBtn;

    private JTextArea infoArea;
    private JTextField speedField;
    private int autoSpeed = 500;

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
                        infoArea.setText("Стартовая вершина: " + startVertex.getName() + ".");
                        setMode("auto");
                    } else {
                        infoArea.setText("Кликните по вершине, чтобы выбрать старт.");
                    }
                } else if (currentMode.equals("selectEnd")) {
                    if (clicked != null && !clicked.equals(startVertex)) {
                        endVertex = clicked;
                        canvas.setEndVertex(endVertex);
                        canvas.repaint();
                        infoArea.setText("Конечная вершина: " + endVertex.getName() + ".");
                        setMode("auto");
                    } else if (clicked != null && clicked.equals(startVertex)) {
                        infoArea.setText("Стартовая и конечная вершины не должны совпадать.");
                    } else {
                        infoArea.setText("Кликните по вершине, чтобы выбрать конечную вершину.");
                    }
                } else if (currentMode.equals("auto")) {
                    if (result != null && currentStepIndex >= result.getStepsHistory().size()) {
                        if (clicked != null) {
                            showPathToVertex(clicked);
                        } else {
                            canvas.setShortestPath(null);
                            infoArea.setText("Алгоритм завершён. Кликните по вершине, чтобы увидеть кратчайший путь.");
                        }
                    } else {
                        if (clicked == null) {
                            createVertex(x, y);
                        } else {
                            handleEdgeCreation(clicked);
                        }
                    }
                }
            }
        });

        add(canvas, BorderLayout.CENTER);

        addVertexBtn = new JButton("Вершина");
        addEdgeBtn = new JButton("Ребро");
        startBtn = new JButton("Старт");
        finishBtn = new JButton("Финиш");
        runBtn = new JButton("Запустить");
        stepBtn = new JButton("Шаг вперёд");
        resetBtn = new JButton("Сброс");
        arrangeBtn = new JButton("Расставить");
        clearBtn = new JButton("Очистить");
        autoBtn = new JButton("Авто");
        saveLogBtn = new JButton("Сохранить лог");

        JButton[] allButtons = {
            addVertexBtn, addEdgeBtn, startBtn, finishBtn,
            runBtn, stepBtn, resetBtn, arrangeBtn, clearBtn, autoBtn, saveLogBtn
        };
        for (JButton btn : allButtons) {
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            addHoverEffect(btn);
        }

        runBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        resetBtn.setEnabled(false);
        arrangeBtn.setEnabled(true);
        autoBtn.setEnabled(false);
        saveLogBtn.setEnabled(false);

        addVertexBtn.addActionListener(e -> {
            resetAlgorithmIfRunning();
            setMode("addVertex");
        });
        addEdgeBtn.addActionListener(e -> {
            resetAlgorithmIfRunning();
            setMode("addEdge");
        });
        clearBtn.addActionListener(e -> clearGraph());
        runBtn.addActionListener(e -> runAlgorithm());
        stepBtn.addActionListener(e -> showNextStep());
        resetBtn.addActionListener(e -> resetAlgorithm());
        autoBtn.addActionListener(e -> startAutoPlay());
        saveLogBtn.addActionListener(e -> saveLogToFile());
        arrangeBtn.addActionListener(e -> {
            if (graph.getVertexCount() == 0) {
                // Граф пустой — предлагаем сгенерировать случайный
                String input = JOptionPane.showInputDialog(
                        MainWindow.this,
                        "Граф пуст. Введите количество вершин для случайного графа (5-100):",
                        "20"
                );
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int vertexCount = Integer.parseInt(input.trim());
                        if (vertexCount < 5) vertexCount = 5;
                        if (vertexCount > 100) vertexCount = 100;
                        generateRandomGraph(vertexCount);
                        infoArea.setText("Сгенерирован случайный граф: " + vertexCount + " вершин.");
                    } catch (NumberFormatException ex) {
                        infoArea.setText("Ошибка: введите целое число.");
                    }
                }
                return;
            }

            // Граф не пустой — расставляем
            int worldWidth = (int) (canvas.getWidth() / canvas.getZoom());
            int worldHeight = (int) (canvas.getHeight() / canvas.getZoom());
            graph.layoutForceDirected(worldWidth, worldHeight);
            canvas.repaint();
            infoArea.setText("Вершины перераспределены автоматически (" + graph.getVertexCount() + " вершин).");
        });

        startBtn.addActionListener(e -> {
            resetAlgorithmIfRunning();
            if (currentMode.equals("selectStart")) {
                setMode("auto");
            } else {
                setMode("selectStart");
            }
        });
        finishBtn.addActionListener(e -> {
            if (currentMode.equals("selectEnd")) {
                setMode("auto");
            } else {
                setMode("selectEnd");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(13, 1, 5, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel.add(addVertexBtn);
        buttonPanel.add(addEdgeBtn);
        buttonPanel.add(startBtn);
        buttonPanel.add(finishBtn);
        buttonPanel.add(runBtn);
        buttonPanel.add(stepBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(arrangeBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(saveLogBtn);

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

    private void resetAlgorithmIfRunning() {
        if (result != null || startVertex != null || endVertex != null) {
            resetAlgorithm();
        }
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
               (btn == addEdgeBtn && currentMode.equals("addEdge")) ||
               (btn == startBtn && currentMode.equals("selectStart")) ||
               (btn == finishBtn && currentMode.equals("selectEnd"));
    }

    private void applyModeStyle(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(null);

        if (btn.isEnabled()) {
            if (btn == addVertexBtn && currentMode.equals("addVertex")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            } else if (btn == addEdgeBtn && currentMode.equals("addEdge")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            } else if (btn == startBtn && currentMode.equals("selectStart")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            } else if (btn == finishBtn && currentMode.equals("selectEnd")) {
                btn.setBackground(ACTIVE_COLOR);
                btn.setContentAreaFilled(true);
            }
        }
    }

    private void updateButtonStyles() {
        for (JButton btn : new JButton[]{addVertexBtn, addEdgeBtn, startBtn, finishBtn,
                runBtn, stepBtn, resetBtn, arrangeBtn, clearBtn, autoBtn, saveLogBtn}) {
            applyModeStyle(btn);
        }
    }

    private void checkMouseHover(JButton btn) {
        if (!btn.isEnabled()) return;
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
            } else if (mode.equals("selectStart")) {
                infoArea.setText("Выберите стартовую вершину.");
            } else if (mode.equals("selectEnd")) {
                infoArea.setText("Выберите конечную вершину.");
            } else {
                infoArea.setText("Авто-режим: клик по пустому месту — вершина, клик по вершине — ребро.");
            }
        }
        updateButtonStyles();
        deselectVertex();

        for (JButton btn : new JButton[]{addVertexBtn, addEdgeBtn, startBtn, finishBtn,
                runBtn, stepBtn, resetBtn, arrangeBtn, clearBtn, autoBtn, saveLogBtn}) {
            checkMouseHover(btn);
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

    private void generateRandomGraph(int vertexCount) {
        graph.clear();
        vertexCounter = 0;

        int worldWidth = (int) (canvas.getWidth() / canvas.getZoom());
        int worldHeight = (int) (canvas.getHeight() / canvas.getZoom());

        // Создаём вершины со случайными координатами
        for (int i = 0; i < vertexCount; i++) {
            int x = 50 + (int) (Math.random() * (worldWidth - 100));
            int y = 50 + (int) (Math.random() * (worldHeight - 100));
            Vertex v = new Vertex(vertexCounter, String.valueOf(vertexCounter), x, y);
            vertexCounter++;
            graph.addVertex(v);
        }

        // Создаём случайные рёбра (15% вероятность между любыми двумя вершинами)
        List<Vertex> vertices = graph.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                // Динамическая вероятность: меньше вершин — выше шанс ребра, больше вершин — ниже
                double probability;
                if (vertexCount <= 10) {
                    probability = 0.15;
                } else if (vertexCount <= 20) {
                    probability = 0.05;
                } else if (vertexCount <= 40) {
                    probability = 0.02;
                } else if (vertexCount <= 60) {
                    probability = 0.01;
                } else {
                    probability = 0.02;
                }

                if (i != j && Math.random() < probability) {
                    int weight = 1 + (int) (Math.random() * 20);
                    graph.addEdge(vertices.get(i), vertices.get(j), weight);
                }
            }
        }

        // Автоматически расставляем
        graph.layoutForceDirected(worldWidth, worldHeight);
        canvas.repaint();
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
        updateButtonStyles();
    }

    private Vertex findVertexAt(int x, int y) {
        for (Vertex v : graph.getVertices()) {
            int vx = (int) v.getX();
            int vy = (int) v.getY();
            double dist = Math.sqrt((x - vx) * (x - vx) + (y - vy) * (y - vy));
            if (dist <= 25) return v;
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
        saveLogBtn.setEnabled(true);
        runBtn.setEnabled(false);
        updateButtonStyles();
    }

    private void showNextStep() {
        if (result == null) return;

        if (currentStepIndex < result.getStepsHistory().size()) {
            BellmanFordStep step = result.getStepsHistory().get(currentStepIndex);
            canvas.setCurrentStep(step);
            infoArea.setText(step.getStepDescription());
            currentStepIndex++;
        }

        if (currentStepIndex >= result.getStepsHistory().size()) {
            showFinalPath();
            stepBtn.setEnabled(false);
            autoBtn.setEnabled(false);
            canvas.setCurrentStep(null);
            canvas.repaint();
            updateButtonStyles();
        }
    }

    private void resetAlgorithm() {
        if (autoTimer != null) autoTimer.stop();
        autoBtn.setEnabled(false);
        saveLogBtn.setEnabled(false);
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
        updateButtonStyles();
    }

    private void startAutoPlay() {
        if (result == null) return;

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
                canvas.setCurrentStep(step);
                infoArea.setText(step.getStepDescription());
                currentStepIndex++;
            } else {
                autoTimer.stop();
                showFinalPath();
                stepBtn.setEnabled(false);
                autoBtn.setEnabled(false);
                canvas.setCurrentStep(null);
                canvas.repaint();
                updateButtonStyles();
            }
        });
        autoTimer.start();
    }

    private void showPathToVertex(Vertex v) {
        int dist = result.getFinalDistances().get(v);
        if (result.hasNegativeCycle() && result.getUnreachableVertices().contains(v)) {
            infoArea.setText("Вершина " + v.getName() + " находится в отрицательном цикле. Путь не существует.");
            canvas.setShortestPath(null);
        } else if (dist == 1_000_000_000) {
            infoArea.setText("Путь до вершины " + v.getName() + " не найден.");
            canvas.setShortestPath(null);
        } else {
            List<Vertex> path = algorithm.getShortestPath(v, result.getPredecessors(), result.getFinalDistances());
            canvas.setShortestPath(path);
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) pathStr.append(" -> ");
                pathStr.append(path.get(i).getName());
            }
            infoArea.setText("Кратчайший путь до " + v.getName() + ": " + pathStr + ". Длина: " + dist + ".");
        }
        canvas.repaint();
    }

    private void showFinalPath() {
        if (result == null) return;

        if (endVertex != null) {
            showPathToVertex(endVertex);
        } else {
            canvas.setShortestPath(null);
            infoArea.setText("Алгоритм завершён. Кликните по вершине, чтобы увидеть кратчайший путь.");
            canvas.repaint();
        }
    }

    private void saveLogToFile() {
        if (result == null) {
            infoArea.setText("Нет данных для сохранения.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить лог алгоритма");
        fileChooser.setSelectedFile(new File("bellman_ford_log.txt"));

        int userChoice = fileChooser.showSaveDialog(MainWindow.this);
        if (userChoice != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("=== Лог алгоритма Форда-Беллмана ===");
            writer.println("Стартовая вершина: " + startVertex.getName());
            writer.println("Конечная вершина: " + (endVertex != null ? endVertex.getName() : "не выбрана"));
            writer.println("Количество вершин в графе: " + graph.getVertexCount());
            writer.println("Всего шагов: " + result.getStepsHistory().size());
            writer.println("Отрицательный цикл: " + (result.hasNegativeCycle() ? "обнаружен" : "не обнаружен"));
            writer.println();

            List<BellmanFordStep> history = result.getStepsHistory();
            for (int i = 0; i < history.size(); i++) {
                BellmanFordStep step = history.get(i);
                writer.println("Шаг " + (i + 1) + ": " + step.getStepDescription());
            }

            writer.println();
            writer.println("=== Итоговые расстояния ===");
            Map<Vertex, Integer> distances = result.getFinalDistances();
            for (Vertex v : graph.getVertices()) {
                int dist = distances.get(v);
                String distStr = (dist == 1_000_000_000) ? "недостижима" : String.valueOf(dist);
                if (result.hasNegativeCycle() && result.getUnreachableVertices().contains(v)) {
                    distStr = "в отрицательном цикле";
                }
                writer.println("Вершина " + v.getName() + ": " + distStr);
            }

            infoArea.setText("Лог успешно сохранён в " + file.getAbsolutePath());
        } catch (IOException ex) {
            infoArea.setText("Ошибка сохранения: " + ex.getMessage());
        }
    }
}