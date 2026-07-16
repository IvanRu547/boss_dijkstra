//Класс самого графа
package model;

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
        // 1. Получаем все рёбра графа
        List<Edge> allEdges = getAllEdges();

        // 2. Собираем рёбра, связанные с удаляемой вершиной
        List<Edge> toRemove = new ArrayList<>();
        for (Edge e : allEdges) {
            if (e.getFrom().equals(v) || e.getTo().equals(v)) {
                toRemove.add(e);
            }
        }

        // 3. Удаляем эти рёбра из всех списков смежности
        for (List<Edge> edges : adjacencyMap.values()) {
            edges.removeAll(toRemove);
        }

        // 4. Удаляем саму вершину
        adjacencyMap.remove(v);
        vertices.remove(v);
    }

    public void addEdge(Vertex from, Vertex to, int weight) {
        // Проверяем, есть ли уже прямое ребро
        Edge existing = getEdgeBetween(from, to);
        if (existing != null) {
            existing.setWeight(weight);
            return;
        }

        // Если прямого нет, создаём новое
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

    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (List<Edge> edges : adjacencyMap.values()) {
            for (Edge e : edges) {
                if (!allEdges.contains(e)) {
                    allEdges.add(e);
                }
            }
        }
        return allEdges;
    }

    public void layoutForceDirected(int width, int height) {
        int vertexCount = vertices.size();
        if (vertexCount == 0) return;

        int iterations = 100;
        if (vertexCount > 30) iterations = 200;
        if (vertexCount > 70) iterations = 300;

        double area = width * height;
        double k = Math.sqrt(area / vertexCount);
        double minDistance = 60.0; // минимальное расстояние между центрами вершин
        int margin = 50;

        double temperature = width / 10.0;
        double coolingFactor = 0.95;

        // Случайные начальные позиции
        for (Vertex v : vertices) {
            v.setX(width / 2.0 + (Math.random() - 0.5) * width / 2.0);
            v.setY(height / 2.0 + (Math.random() - 0.5) * height / 2.0);
        }

        for (int iter = 0; iter < iterations; iter++) {
            double[] dispX = new double[vertexCount];
            double[] dispY = new double[vertexCount];

            // Силы отталкивания между всеми парами вершин
            for (int i = 0; i < vertexCount; i++) {
                Vertex vi = vertices.get(i);
                for (int j = i + 1; j < vertexCount; j++) {
                    Vertex vj = vertices.get(j);
                    double dx = vi.getX() - vj.getX();
                    double dy = vi.getY() - vj.getY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 1.0) dist = 1.0;

                    double force = k * k / dist;
                    double fx = (dx / dist) * force;
                    double fy = (dy / dist) * force;

                    dispX[i] += fx;
                    dispY[i] += fy;
                    dispX[j] -= fx;
                    dispY[j] -= fy;
                }
            }

            // Силы притяжения по рёбрам
            for (Edge edge : getAllEdges()) {
                Vertex from = edge.getFrom();
                Vertex to = edge.getTo();
                int i = vertices.indexOf(from);
                int j = vertices.indexOf(to);
                if (i < 0 || j < 0) continue;

                double dx = to.getX() - from.getX();
                double dy = to.getY() - from.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 1.0) dist = 1.0;

                double force = (dist * dist) / k;
                double fx = (dx / dist) * force;
                double fy = (dy / dist) * force;

                dispX[i] += fx;
                dispY[i] += fy;
                dispX[j] -= fx;
                dispY[j] -= fy;
            }

            // Применяем смещение
            for (int i = 0; i < vertexCount; i++) {
                Vertex v = vertices.get(i);
                double dx = dispX[i];
                double dy = dispY[i];
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 1.0) continue;

                double limitedDist = Math.min(dist, temperature);
                double newX = v.getX() + (dx / dist) * limitedDist;
                double newY = v.getY() + (dy / dist) * limitedDist;

                newX = Math.max(margin, Math.min(width - margin, newX));
                newY = Math.max(margin, Math.min(height - margin, newY));

                v.setX(newX);
                v.setY(newY);
            }

            temperature *= coolingFactor;
        }

        // Финальный проход: расталкиваем слипшиеся вершины
        for (int i = 0; i < vertexCount; i++) {
            Vertex vi = vertices.get(i);
            for (int j = i + 1; j < vertexCount; j++) {
                Vertex vj = vertices.get(j);
                double dx = vi.getX() - vj.getX();
                double dy = vi.getY() - vj.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < minDistance && dist > 0.1) {
                    double pushForce = (minDistance - dist) / 2.0;
                    double pushX = (dx / dist) * pushForce;
                    double pushY = (dy / dist) * pushForce;

                    vi.setX(Math.max(margin, Math.min(width - margin, vi.getX() + pushX)));
                    vi.setY(Math.max(margin, Math.min(height - margin, vi.getY() + pushY)));
                    vj.setX(Math.max(margin, Math.min(width - margin, vj.getX() - pushX)));
                    vj.setY(Math.max(margin, Math.min(height - margin, vj.getY() - pushY)));
                }
            }
        }
    }
}
