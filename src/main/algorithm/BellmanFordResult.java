package algorithm;

import model.Vertex;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BellmanFordResult {
    private final List<BellmanFordStep> stepsHistory;
    private final Map<Vertex, Integer> finalDistances;
    private final Map<Vertex, Vertex> predecessors;
    private final boolean hasNegativeCycle;
    private final Set<Vertex> unreachableVertices;

    public BellmanFordResult(List<BellmanFordStep> stepsHistory,
                             Map<Vertex, Integer> finalDistances,
                             Map<Vertex, Vertex> predecessors,
                             boolean hasNegativeCycle,
                             Set<Vertex> unreachableVertices) {
        this.stepsHistory = stepsHistory;
        this.finalDistances = finalDistances;
        this.predecessors = predecessors;
        this.hasNegativeCycle = hasNegativeCycle;
        this.unreachableVertices = unreachableVertices; // Вершины, путь до которых испорчен циклом
    }

    public List<BellmanFordStep> getStepsHistory() { return stepsHistory; }
    public Map<Vertex, Integer> getFinalDistances() { return finalDistances; }
    public Map<Vertex, Vertex> getPredecessors() { return predecessors; }
    public boolean hasNegativeCycle() { return hasNegativeCycle; }
    public Set<Vertex> getUnreachableVertices() { return unreachableVertices; }
}