package algorithm;
import model.Vertex;
import model.Edge;
import model.Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BellmanFordTester {

    private static final String LOG_FILE = "bellman_ford_test_log.txt";

    public static void main(String[] args) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE))) {
            writer.println("=== СТАРТ ТЕСТИРОВАНИЯ АЛГОРИТМА ФОРДА-БЕЛЛМАНА ===");
            writer.println();

            testSimpleGraph(writer);
            testGraphWithNegativeWeights(writer);
            testGraphWithNegativeCycle(writer);
            testDisconnectedGraph(writer);

            writer.println("=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
            System.out.println("Логи успешно записаны в файл: " + LOG_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка при записи лог-файла: " + e.getMessage());
        }
    }

    private static void testSimpleGraph(PrintWriter writer) {
        writer.println("--- ТЕСТ 1: Простой ориентированный граф (без отрицательных весов) ---");
        Graph graph = new Graph();
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 10, 10);
        Vertex v3 = new Vertex(3, "C", 20, 20);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 5);
        graph.addEdge(v2, v3, 3);
        graph.addEdge(v1, v3, 10);

        runAndLogTest(graph, v1, v3, writer);
    }

    private static void testGraphWithNegativeWeights(PrintWriter writer) {
        writer.println("--- ТЕСТ 2: Граф с отрицательными весами (без цикла) ---");
        Graph graph = new Graph();
        Vertex v1 = new Vertex(1, "S", 0, 0);
        Vertex v2 = new Vertex(2, "A", 0, 0);
        Vertex v3 = new Vertex(3, "B", 0, 0);
        Vertex v4 = new Vertex(4, "C", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);

        graph.addEdge(v1, v2, 4);
        graph.addEdge(v1, v3, 5);
        graph.addEdge(v3, v2, -2);
        graph.addEdge(v2, v4, 3);

        runAndLogTest(graph, v1, v4, writer);
    }

    private static void testGraphWithNegativeCycle(PrintWriter writer) {
        writer.println("--- ТЕСТ 3: Граф с отрицательным циклом ---");
        Graph graph = new Graph();
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        Vertex v3 = new Vertex(3, "C", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 1);
        graph.addEdge(v2, v3, -1);
        graph.addEdge(v3, v1, -1);

        runAndLogTest(graph, v1, v3, writer);
    }

    private static void testDisconnectedGraph(PrintWriter writer) {
        writer.println("--- ТЕСТ 4: Несвязный граф ---");
        Graph graph = new Graph();
        Vertex v1 = new Vertex(1, "Start", 0, 0);
        Vertex v2 = new Vertex(2, "Reachable", 0, 0);
        Vertex v3 = new Vertex(3, "Unreachable", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 5);

        runAndLogTest(graph, v1, v3, writer);
    }

    private static void runAndLogTest(Graph graph, Vertex startVertex, Vertex targetVertex, PrintWriter writer) {
        writer.println("ВХОДНОЙ ГРАФ:");
        writer.print("  Вершины: ");
        List<Vertex> vertices = graph.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            writer.print(vertices.get(i).getName());
            if (i < vertices.size() - 1) {
                writer.print(", ");
            }
        }
        writer.println();

        writer.println("  Ориентированные ребра:");
        boolean hasEdges = false;
        for (Vertex v : vertices) {
            List<Edge> neighbors = graph.getNeighbors(v);
            for (Edge edge : neighbors) {
                writer.println("    " + edge.getFrom().getName() + " -> " + edge.getTo().getName() + " (вес: " + edge.getWeight() + ")");
                hasEdges = true;
            }
        }
        if (!hasEdges) {
            writer.println("    (в графе нет ребер)");
        }
        writer.println();

        BellmanFordAlgorithm algorithm = new BellmanFordAlgorithm();
        BellmanFordResult result = algorithm.execute(graph, startVertex);

        writer.println("Стартовая вершина: " + startVertex.getName());
        writer.println("Целевая вершина для проверки пути: " + targetVertex.getName());

        boolean hasCycle = result.hasNegativeCycle();
        writer.println("Отрицательный цикл обнаружен: " + hasCycle);

        writer.println("\nФинальные дистанции:");
        Map<Vertex, Integer> distances = result.getFinalDistances();
        for (Map.Entry<Vertex, Integer> entry : distances.entrySet()) {
            String distStr = entry.getValue() == Integer.MAX_VALUE ? "Infinity" : String.valueOf(entry.getValue());
            writer.println("До " + entry.getKey().getName() + " = " + distStr);
        }

        writer.println("\nНедостижимые вершины:");
        Set<Vertex> unreachable = result.getUnreachableVertices();
        if (unreachable.isEmpty()) {
            writer.println("- Нет недостижимых вершин.");
        } else {
            for (Vertex v : unreachable) {
                writer.println("- " + v.getName());
            }
        }

        if (!hasCycle) {
            writer.println("\nВосстановление пути до " + targetVertex.getName() + ":");
            List<Vertex> path = algorithm.getShortestPath(targetVertex, result.getPredecessors(), distances);
            if (path.isEmpty()) {
                writer.println("Путь не существует (возвращен пустой список).");
            } else {
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 0; i < path.size(); i++) {
                    pathBuilder.append(path.get(i).getName());
                    if (i < path.size() - 1) pathBuilder.append(" -> ");
                }
                writer.println(pathBuilder.toString());
            }
        } else {
            writer.println("\nВосстановление пути пропущено, так как обнаружен отрицательный цикл.");
        }

        writer.println("\nИстория шагов (детализация релаксации):");
        List<BellmanFordStep> history = result.getStepsHistory();
        for (int i = 0; i < history.size(); i++) {
            BellmanFordStep step = history.get(i);
            writer.println("Шаг " + (i + 1) + ":");
            writer.println("  Описание: " + step.getStepDescription());
            writer.println("  Активная вершина: " + step.getActiveVertex().getName());
            writer.println("  Дистанция обновлена: " + step.isDistanceUpdated());
            Edge edge = step.getProcessedEdge();
            if (edge != null) {
                writer.println("  Обрабатываемое ребро: " + edge.getFrom().getName() + " -> " + edge.getTo().getName() + " (вес: " + edge.getWeight() + ")");
            }
        }
        writer.println("--------------------------------------------------\n");
    }
}