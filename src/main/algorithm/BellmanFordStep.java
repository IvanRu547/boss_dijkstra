package algorithm;
import model.Edge;
import model.Vertex;
import java.util.HashMap;
import java.util.Map;

public class BellmanFordStep {
    private final Edge processedEdge;
    private final Vertex activeVertex;
    private final Map<Vertex, Integer> distancesSnapshot;
    private final Map<Vertex, Vertex> predecessorsSnapshot;
    private final boolean isDistanceUpdated;
    private final String stepDescription;

    public BellmanFordStep(Edge processedEdge,
                           Vertex activeVertex,
                           Map<Vertex, Integer> currentDistances,
                           Map<Vertex, Vertex> currentPredecessors,
                           boolean isDistanceUpdated,
                           String stepDescription) {
        this.processedEdge = processedEdge;
        this.activeVertex = activeVertex;
        // Обязательно делаем копию состояния на текущий момент!
        this.distancesSnapshot = new HashMap<>(currentDistances);
        this.predecessorsSnapshot = new HashMap<>(currentPredecessors);
        this.isDistanceUpdated = isDistanceUpdated;
        this.stepDescription = stepDescription; // Подробный текст шага на русском
    }

    public Edge getProcessedEdge() { return processedEdge; }
    public Vertex getActiveVertex() { return activeVertex; }
    public Map<Vertex, Integer> getDistancesSnapshot() { return distancesSnapshot; }
    public Map<Vertex, Vertex> getPredecessorsSnapshot() { return predecessorsSnapshot; }
    public boolean isDistanceUpdated() { return isDistanceUpdated; }
    public String getStepDescription() { return stepDescription; }
}