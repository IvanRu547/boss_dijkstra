//Класс ребра графа
package main.model;

public class Edge {
    private Vertex from;
    private Vertex to;
    private int weight;

    public Edge(Vertex from, Vertex to, int weight){
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Vertex getFrom(){return this.from;}
    public Vertex getTo(){return this.to;}
    public int getWeight(){return this.weight;}
}
