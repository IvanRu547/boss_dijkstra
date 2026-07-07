//Класс самого графа
package main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private List<Vertex> vertices;
    private Map<Vertex, List<Edge>> adjacencyMap;

    public Graph() {
        this.vertices = new ArrayList<>();
        this.adjacencyMap = new HashMap<>();
    }

    public void addVertex(Vertex v) {
        vertices.add(v);
        adjacencyMap.put(v, new ArrayList<>());
    }

    public void removeVertex(Vertex v) {
        // 1. Собираем все рёбра, связанные с удаляемой вершиной
        List<Edge> toRemove = new ArrayList<>();
        for (List<Edge> edges : adjacencyMap.values()) {
            for (Edge e : edges) {
                if (e.getFrom().equals(v) || e.getTo().equals(v)) {
                    toRemove.add(e);
                }
            }
        }

        // 2. Удаляем эти рёбра из всех списков смежности
        for (List<Edge> edges : adjacencyMap.values()) {
            edges.removeAll(toRemove);
        }

        // 3. Удаляем саму вершину
        adjacencyMap.remove(v);
        vertices.remove(v);
    }

    public void addEdge(Vertex from, Vertex to, int weight) {
        if (!adjacencyMap.containsKey(from) || !adjacencyMap.containsKey(to)) {
            return; // или throw new IllegalArgumentException("Вершина не найдена")
        }
        Edge edge = new Edge(from, to, weight);
        adjacencyMap.get(from).add(edge);
    }

    public void removeEdge(Edge e) {
        adjacencyMap.get(e.getFrom()).remove(e);
    }

    public List<Edge> getNeighbors(Vertex v) {
        return adjacencyMap.get(v);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public Vertex getVertexById(int id) {
        for (Vertex v : vertices) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    public Edge getEdgeBetween(Vertex from, Vertex to) {
        for (Edge e : adjacencyMap.get(from)) {
            if (e.getTo().equals(to)) {
                return e;
            }
        }
        return null;
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public void clear() {
        vertices.clear();
        adjacencyMap.clear();
    }
}
