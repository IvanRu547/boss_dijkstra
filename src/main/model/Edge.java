//Класс ребра графа
package model;

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
    public void setWeight(int weight) { this.weight = weight; }
}
