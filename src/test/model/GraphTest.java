package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class GraphTest {
    private Graph graph;
    private Vertex v1, v2, v3;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        v1 = new Vertex(1, "A", 0, 0);
        v2 = new Vertex(2, "B", 10, 10);
        v3 = new Vertex(3, "C", 20, 20);
    }

    @Test
    void testAddAndGetVertex() {
        graph.addVertex(v1);
        assertEquals(1, graph.getVertexCount(), "Количество вершин должно быть 1");
        assertEquals(v1, graph.getVertexById(1), "Вершина должна находиться по ID");
    }

    @Test
    void testAddEdgeAndUpdateWeight() {
        graph.addVertex(v1);
        graph.addVertex(v2);

        // Добавляем ребро
        graph.addEdge(v1, v2, 5);
        Edge edge = graph.getEdgeBetween(v1, v2);

        assertNotNull(edge, "Ребро должно существовать");
        assertEquals(5, edge.getWeight(), "Вес ребра должен быть 5");

        // Обновляем вес существующего ребра
        graph.addEdge(v1, v2, 10);
        assertEquals(10, edge.getWeight(), "Вес ребра должен обновиться до 10");
        assertEquals(1, graph.getNeighbors(v1).size(), "Новое ребро не должно создаваться, только обновление");
    }

    @Test
    void testIndependentReverseEdges() {
        graph.addVertex(v1);
        graph.addVertex(v2);

        graph.addEdge(v1, v2, 5);
        graph.addEdge(v2, v1, 15);

        assertEquals(5, graph.getEdgeBetween(v1, v2).getWeight());
        assertEquals(15, graph.getEdgeBetween(v2, v1).getWeight());
    }

    @Test
    void testRemoveVertex() {
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);

        graph.addEdge(v1, v2, 5);
        graph.addEdge(v2, v3, 10);
        graph.addEdge(v1, v3, 15);

        graph.removeVertex(v2);

        assertEquals(2, graph.getVertexCount(), "Должно остаться 2 вершины");
        assertNull(graph.getVertexById(2), "Вершина B должна быть удалена");

        List<Edge> neighborsV1 = graph.getNeighbors(v1);
        assertEquals(1, neighborsV1.size(), "У вершины A должно остаться только 1 ребро");
        assertEquals(v3, neighborsV1.get(0).getTo(), "Оставшееся ребро должно вести в C");
    }

    @Test
    void testClearGraph() {
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(v1, v2, 5);

        graph.clear();

        assertEquals(0, graph.getVertexCount(), "После очистки граф должен быть пустым");
        assertNull(graph.getVertexById(1), "Вершины не должно существовать");
    }

    @Test
    void testIsolatedVertexNeighbors() {
        Vertex v1 = new Vertex(1, "A", 0, 0);
        graph.addVertex(v1);

        assertNotNull(graph.getNeighbors(v1), "Метод не должен возвращать null для изолированной вершины");
        assertTrue(graph.getNeighbors(v1).isEmpty(), "Список соседей должен быть пуст");
    }

    @Test
    void testVertexGettersAndSetters() {
        Vertex v = new Vertex(1, "TestVertex", 10.0, 20.0);
        assertEquals("TestVertex", v.getName());
        assertEquals(10.0, v.getX(), 0.001); // 0.001 — погрешность для double
        assertEquals(20.0, v.getY(), 0.001);

        v.setX(50.0);
        v.setY(100.0);

        assertEquals(50.0, v.getX(), 0.001, "Координата X должна обновиться");
        assertEquals(100.0, v.getY(), 0.001, "Координата Y должна обновиться");
    }

    @Test
    void testGetVerticesSafety() {
        graph.addVertex(v1);
        List<Vertex> vertices = graph.getVertices();

        assertEquals(1, vertices.size());

        assertTrue(vertices.contains(v1));
        assertNotNull(vertices);
    }

    @Test
    void testRemoveEdge() {
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(v1, v2, 5);

        Edge edge = graph.getEdgeBetween(v1, v2);
        assertNotNull(edge);

        // Удаляем ребро
        graph.removeEdge(edge);

        // Проверяем, что теперь между ними нет ребра
        assertNull(graph.getEdgeBetween(v1, v2), "Ребро должно быть удалено из графа");
        assertTrue(graph.getNeighbors(v1).isEmpty(), "Список соседей должен быть пуст после удаления единственного ребра");
    }
}