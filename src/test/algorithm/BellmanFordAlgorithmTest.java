package algorithm;

import model.Edge;
import model.Graph;
import model.Vertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

class BellmanFordAlgorithmTest {
    private static final int INF = 1_000_000_000;
    private Graph graph;
    private BellmanFordAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        algorithm = new BellmanFordAlgorithm();
    }

    @Test
    void testSimpleGraph() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        Vertex v3 = new Vertex(3, "C", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 5);
        graph.addEdge(v2, v3, 3);
        graph.addEdge(v1, v3, 10);

        BellmanFordResult result = algorithm.execute(graph, v1);

        assertFalse(result.hasNegativeCycle(), "Отрицательного цикла быть не должно");

        Map<Vertex, Integer> distances = result.getFinalDistances();
        assertEquals(0, distances.get(v1));
        assertEquals(5, distances.get(v2));
        assertEquals(8, distances.get(v3)); // Путь A -> B -> C дешевле, чем A -> C напрямую

        List<Vertex> path = algorithm.getShortestPath(v3, result.getPredecessors(), distances);
        assertEquals(List.of(v1, v2, v3), path, "Кратчайший путь должен быть A -> B -> C");
    }

    @Test
    void testGraphWithNegativeWeights() {
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

        BellmanFordResult result = algorithm.execute(graph, v1);

        assertFalse(result.hasNegativeCycle());
        Map<Vertex, Integer> distances = result.getFinalDistances();

        assertEquals(0, distances.get(v1));
        assertEquals(3, distances.get(v2)); // Путь S -> B -> A (5 - 2 = 3)
        assertEquals(5, distances.get(v3));
        assertEquals(6, distances.get(v4)); // Путь S -> B -> A -> C (3 + 3 = 6)
    }

    @Test
    void testGraphWithNegativeCycle() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        Vertex v3 = new Vertex(3, "C", 0, 0);
        Vertex v4 = new Vertex(4, "Safe", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);

        // Создаем отрицательный цикл A -> B -> C -> A
        graph.addEdge(v1, v2, 1);
        graph.addEdge(v2, v3, -1);
        graph.addEdge(v3, v1, -1);
        // Вершина вне цикла
        graph.addEdge(v1, v4, 10);

        BellmanFordResult result = algorithm.execute(graph, v1);

        assertTrue(result.hasNegativeCycle(), "Алгоритм должен обнаружить отрицательный цикл [cite: 14]");

        Set<Vertex> unreachable = result.getUnreachableVertices();
        assertTrue(unreachable.contains(v1));
        assertTrue(unreachable.contains(v2));
        assertTrue(unreachable.contains(v3));
        assertTrue(unreachable.contains(v4));
    }

    @Test
    void testDisconnectedGraph() {
        Vertex v1 = new Vertex(1, "Start", 0, 0);
        Vertex v2 = new Vertex(2, "Reachable", 0, 0);
        Vertex v3 = new Vertex(3, "Unreachable", 0, 0);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 5);

        BellmanFordResult result = algorithm.execute(graph, v1);
        Map<Vertex, Integer> distances = result.getFinalDistances();

        assertEquals(0, distances.get(v1));
        assertEquals(5, distances.get(v2));
        assertEquals(INF, distances.get(v3), "Для несвязанных вершин алгоритм должен вернуть бесконечность ");

        List<Vertex> path = algorithm.getShortestPath(v3, result.getPredecessors(), distances);
        assertTrue(path.isEmpty(), "Путь к недостижимой вершине должен быть пустым списком [cite: 6]");
    }

    @Test
    void testSingleVertexGraph() {
        Vertex v1 = new Vertex(1, "Single", 0, 0);
        graph.addVertex(v1);

        BellmanFordResult result = algorithm.execute(graph, v1);

        assertEquals(0, result.getFinalDistances().get(v1), "Расстояние до самой себя должно быть 0");
        assertFalse(result.hasNegativeCycle());
    }

    @Test
    void testLongChain() {
        Vertex v1 = new Vertex(1, "1", 0, 0);
        Vertex v2 = new Vertex(2, "2", 0, 0);
        Vertex v3 = new Vertex(3, "3", 0, 0);
        graph.addVertex(v1); graph.addVertex(v2); graph.addVertex(v3);

        graph.addEdge(v1, v2, 1);
        graph.addEdge(v2, v3, 1);

        BellmanFordResult result = algorithm.execute(graph, v1);
        assertEquals(2, result.getFinalDistances().get(v3), "Алгоритм должен найти путь длиной 2");
    }

    @Test
    void testStepsHistoryCapture() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        graph.addVertex(v1); graph.addVertex(v2);
        graph.addEdge(v1, v2, 5);

        BellmanFordResult result = algorithm.execute(graph, v1);
        List<BellmanFordStep> history = result.getStepsHistory();

        assertFalse(history.isEmpty(), "История шагов должна записываться");

        boolean foundUpdate = history.stream().anyMatch(BellmanFordStep::isDistanceUpdated);
        assertTrue(foundUpdate, "В истории должен быть шаг с обновлением дистанции");
    }

    @Test
    void testPathRecovery() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        Vertex v3 = new Vertex(3, "C", 0, 0);
        graph.addVertex(v1); graph.addVertex(v2); graph.addVertex(v3);
        graph.addEdge(v1, v2, 2);
        graph.addEdge(v2, v3, 3);

        BellmanFordResult result = algorithm.execute(graph, v1);
        List<Vertex> path = algorithm.getShortestPath(v3, result.getPredecessors(), result.getFinalDistances());

        assertEquals(3, path.size(), "Путь должен содержать 3 вершины");
        assertEquals(v1, path.get(0));
        assertEquals(v3, path.get(2));
    }

    @Test
    void testStepDataIntegrity() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        graph.addVertex(v1); graph.addVertex(v2);
        graph.addEdge(v1, v2, 5);

        BellmanFordResult result = algorithm.execute(graph, v1);

        BellmanFordStep step = result.getStepsHistory().get(0);

        assertNotNull(step.getStepDescription(), "Описание шага не должно быть пустым");
        assertNotNull(step.getDistancesSnapshot(), "Снимок расстояний должен быть доступен");
        assertNotNull(step.getPredecessorsSnapshot(), "Снимок предшественников должен быть доступен");

        assertTrue(step.getDistancesSnapshot().containsKey(v2), "В снимке должна быть информация про все вершины");
    }

    @Test
    void testStepStateConsistency() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        Vertex v2 = new Vertex(2, "B", 0, 0);
        graph.addVertex(v1); graph.addVertex(v2);
        graph.addEdge(v1, v2, 5);

        BellmanFordResult result = algorithm.execute(graph, v1);

        for (BellmanFordStep step : result.getStepsHistory()) {
            assertNotNull(step.getActiveVertex(), "На активном шаге всегда должна быть активная вершина");

            if (step.isDistanceUpdated()) {
                assertNotNull(step.getProcessedEdge(), "Если дистанция обновилась, значит, обрабатывалось ребро");
            }
        }
    }
}