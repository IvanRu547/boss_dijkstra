package algorithm;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

public class BellmanFordAlgorithm {
    private static final int INF = 1_000_000_000;
    /**
     * Главный метод запуска алгоритма.
     * @param graph Граф, в котором ищем пути.
     * @param startVertex Стартовая вершина.
     * @return Объект с историей шагов и итоговыми результатами.
     */
    public BellmanFordResult execute(Graph graph, Vertex startVertex) {
        // Подготовка структур данных
        List<BellmanFordStep> history = new ArrayList<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Vertex> predecessors = new HashMap<>();
        Set<Vertex> unreachable = new HashSet<>();
        boolean hasNegativeCycle = false;

        // Инициализация (устанавливаем всем бесконечность, стартовой - 0)
        for (Vertex v : graph.getVertices()) {
            distances.put(v, INF);
        }
        distances.put(startVertex, 0);

        // Получаем удобный плоский список всех рёбер
        List<Edge> allEdges = graph.getAllEdges();
        int vCount = graph.getVertexCount(); // [cite: 32]

        for (int i = 0; i < vCount - 1; i++) {
            boolean anyUpdate = false; // Флаг: было ли хоть одно изменение на этой итерации

            for (Edge edge : allEdges) {
                Vertex u = edge.getFrom();
                Vertex v = edge.getTo();
                int weight = edge.getWeight();

                // Проверка: если путь до u найден и через u путь до v короче
                if (distances.get(u) != INF &&
                        distances.get(u) + weight < distances.get(v)) {

                    // Запоминаем старое значение для описания шага
                    int oldDist = distances.get(v);
                    int newDist = distances.get(u) + weight;

                    // Обновляем данные
                    distances.put(v, newDist);
                    predecessors.put(v, u);
                    anyUpdate = true;

                    // Формируем описание для визуализатора
                    String desc = String.format("Итерация %d. Ребро %s->%s (вес %d). " +
                                    "Путь до %s улучшен: %d -> %d.",
                            i + 1, u.getName(), v.getName(), weight,
                            v.getName(), oldDist, newDist);

                    // Сохраняем «снимок» момента
                    history.add(new BellmanFordStep(edge, u, distances, predecessors, true, desc));
                }
            }

            // Если за всю итерацию не было обновлений, можно досрочно выйти из алгоритма
            if (!anyUpdate) break;
        }

        // 2. Дополнительная итерация для поиска отрицательного цикла
        for (Edge edge : allEdges) {
            Vertex u = edge.getFrom();
            Vertex v = edge.getTo();
            int weight = edge.getWeight();

            // Если всё ещё можем улучшить — значит, есть цикл
            if (distances.get(u) != INF && distances.get(u) + weight < distances.get(v)) {
                hasNegativeCycle = true;

                // Начинаем поиск всех вершин, достижимых из этого цикла
                // Запускаем DFS от вершины 'u' или 'v'
                findUnreachable(u, graph, unreachable);
            }
        }

        return new BellmanFordResult(history, distances, predecessors, hasNegativeCycle, unreachable);
    }

    private void findUnreachable(Vertex start, Graph graph, Set<Vertex> unreachable) {
        // Используем простой DFS (поиск в глубину)
        Stack<Vertex> stack = new Stack<>();
        stack.push(start);
        unreachable.add(start);

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();

            for (Edge edge : graph.getNeighbors(current)) {
                Vertex neighbor = edge.getTo();
                // Если мы еще не помечали эту вершину как зараженную
                if (!unreachable.contains(neighbor)) {
                    unreachable.add(neighbor);
                    stack.push(neighbor);
                }
            }
        }
    }

    /**
     * Вспомогательный метод для восстановления кратчайшего пути по массиву предшественников.
     */
    public List<Vertex> getShortestPath(Vertex target, Map<Vertex, Vertex> predecessors, Map<Vertex, Integer> distances) {
        if (distances.getOrDefault(target, Integer.MAX_VALUE) == INF) {
            return new ArrayList<>(); // Возвращаем пустой список, пути нет
        }

        List<Vertex> path = new ArrayList<>();
        Vertex current = target;

        // Идем назад от target, пока вершина есть в карте предков
        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }
        Collections.reverse(path);

        return path;
    }
}