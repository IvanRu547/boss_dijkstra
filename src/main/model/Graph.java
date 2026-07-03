//Класс самого графа
package main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private List<Vertex> vertices;
    private Map<Vertex, List<Edge>> adjacencyMap;

    public Graph(){
        this.vertices = new ArrayList<>();
        this.adjacencyMap = new HashMap<>();
    }

    public void addVertex(Vertex v){
        Graph Graph = new Graph();
        Graph.vertices.add(v);
    }

    public void removeVertex(Vertex v) {
        // 1. Получаем всех соседей удаляемой вершины
        List<Edge> neighbors = adjacencyMap.get(v);

        // 2. У каждого соседа удаляем рёбра, связанные с v
        for (Edge edge : neighbors) {
            Vertex neighbor;
            if (edge.getFrom().equals(v)) {
                neighbor = edge.getTo();
            } else {
                neighbor = edge.getFrom();
            }

            List<Edge> neighborEdges = adjacencyMap.get(neighbor);

            // Собираем рёбра для удаления (не удаляем во время обхода!)
            List<Edge> toRemove = new ArrayList<>();
            for (Edge e : neighborEdges) {
                if (e.getFrom().equals(v) || e.getTo().equals(v)) {
                    toRemove.add(e);
                }
            }
            neighborEdges.removeAll(toRemove);
        }

        // 3. Удаляем вершину из map и списка
        adjacencyMap.remove(v);
        vertices.remove(v);
    }


}
