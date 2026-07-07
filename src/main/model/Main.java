package main.model;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Проверка класса Graph ===\n");

        // 1. Создаём граф
        Graph graph = new Graph();
        System.out.println("1. Граф создан. Вершин: " + graph.getVertexCount());

        // 2. Создаём вершины
        Vertex a = new Vertex(0, "A", 100, 100);
        Vertex b = new Vertex(1, "B", 300, 100);
        Vertex c = new Vertex(2, "C", 200, 300);

        // 3. Добавляем вершины в граф
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        System.out.println("2. Добавлены вершины A, B, C. Всего вершин: " + graph.getVertexCount());

        // 4. Добавляем рёбра
        graph.addEdge(a, b, 5);
        graph.addEdge(a, c, 3);
        graph.addEdge(b, c, 2);
        System.out.println("3. Добавлены рёбра: A->B(5), A->C(3), B->C(2)");

        // 5. Проверяем соседей
        System.out.println("\n4. Соседи вершины A:");
        for (Edge e : graph.getNeighbors(a)) {
            System.out.println("   Ребро: " + e.getFrom().getName() + " -> " + e.getTo().getName() + " (вес " + e.getWeight() + ")");
        }

        // 6. Проверяем поиск ребра
        Edge found = graph.getEdgeBetween(a, b);
        System.out.println("\n5. Ребро между A и B: " + (found != null ? "найдено (вес " + found.getWeight() + ")" : "не найдено"));

        // 7. Проверяем поиск несуществующего ребра (граф ориентированный)
        Edge notFound = graph.getEdgeBetween(b, a);
        System.out.println("6. Ребро между B и A: " + (notFound != null ? "найдено" : "не найдено (так и должно быть)"));

        // 8. Удаляем вершину
        System.out.println("\n7. Удаляем вершину B...");
        graph.removeVertex(b);
        System.out.println("   Вершин осталось: " + graph.getVertexCount());
        System.out.println("   Соседи вершины A после удаления B:");
        for (Edge e : graph.getNeighbors(a)) {
            System.out.println("   Ребро: " + e.getFrom().getName() + " -> " + e.getTo().getName() + " (вес " + e.getWeight() + ")");
        }

        // 9. Проверяем поиск по id
        Vertex foundVertex = graph.getVertexById(0);
        System.out.println("\n8. Поиск вершины с id=0: " + (foundVertex != null ? foundVertex.getName() : "не найдена"));
        Vertex notFoundVertex = graph.getVertexById(99);
        System.out.println("   Поиск вершины с id=99: " + (notFoundVertex != null ? notFoundVertex.getName() : "не найдена"));

        // 10. Очищаем граф
        graph.clear();
        System.out.println("\n9. Граф очищен. Вершин: " + graph.getVertexCount());

        System.out.println("\n=== Все проверки пройдены ===");
    }
}