package algorithm;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

public class BellmanFordAlgorithm {
    private static final int INF = 1_000_000_000;

    public BellmanFordResult execute(Graph graph, Vertex startVertex) {
        List<BellmanFordStep> history = new ArrayList<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> predecessors = new HashMap<>();
        Set<Vertex> unreachable = new HashSet<>();
        boolean hasNegativeCycle = false;

        for (Vertex v : graph.getVertices()) {
            distances.put(v, INF);
        }
        distances.put(startVertex, 0);

        List<Edge> allEdges = graph.getAllEdges();
        int vCount = graph.getVertexCount();

        for (int i = 0; i < vCount - 1; i++) {
            boolean anyUpdate = false;

            for (Edge edge : allEdges) {
                Vertex u = edge.getFrom();
                Vertex v = edge.getTo();
                int weight = edge.getWeight();

                boolean isUpdated = false;
                String desc;

                if (distances.get(u) != INF && distances.get(u) + weight < distances.get(v)) {
                    int oldDist = distances.get(v);
                    int newDist = distances.get(u) + weight;

                    distances.put(v, newDist);
                    predecessors.put(v, u);
                    anyUpdate = true;
                    isUpdated = true;

                    desc = String.format("Итерация %d. Ребро %s->%s (вес %d). Путь до %s улучшен: %d -> %d.",
                            i + 1, u.getName(), v.getName(), weight, v.getName(), oldDist, newDist);
                } else {
                    String reason = (distances.get(u) == INF) ? "вершина не достижима" : "путь не короче текущего";
                    desc = String.format("Итерация %d. Ребро %s->%s (вес %d). Путь до %s не изменен: %s.",
                            i + 1, u.getName(), v.getName(), weight, v.getName(), reason);
                }

                history.add(new BellmanFordStep(edge, v, distances, predecessors, isUpdated, desc));
            }

            if (!anyUpdate) break;
        }

        for (Edge edge : allEdges) {
            Vertex u = edge.getFrom();
            Vertex v = edge.getTo();
            int weight = edge.getWeight();

            if (distances.get(u) != INF && distances.get(u) + weight < distances.get(v)) {
                hasNegativeCycle = true;
                findUnreachable(u, graph, unreachable);
            }
        }

        return new BellmanFordResult(history, distances, predecessors, hasNegativeCycle, unreachable);
    }

    private void findUnreachable(Vertex start, Graph graph, Set<Vertex> unreachable) {
        Stack<Vertex> stack = new Stack<>();
        stack.push(start);
        unreachable.add(start);

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();

            for (Edge edge : graph.getNeighbors(current)) {
                Vertex neighbor = edge.getTo();
                if (!unreachable.contains(neighbor)) {
                    unreachable.add(neighbor);
                    stack.push(neighbor);
                }
            }
        }
    }

    public List<Vertex> getShortestPath(Vertex target, Map<Vertex, Vertex> predecessors, Map<Vertex, Integer> distances) {
        if (distances.getOrDefault(target, Integer.MAX_VALUE) == INF) {
            return new ArrayList<>();
        }

        List<Vertex> path = new ArrayList<>();
        Set<Vertex> visited = new HashSet<>();
        Vertex current = target;

        while (current != null) {
            if (visited.contains(current)) {
                return new ArrayList<>();
            }
            visited.add(current);
            path.add(current);
            current = predecessors.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}