//Класс вершины графа
package model;

public class Vertex {
    private int id;
    private String name;
    private double x;
    private double y;

    public Vertex(int id, String name, double x, double y){
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }
    public int getId(){return this.id;}
    public String getName(){return this.name;}
    public double getX(){return this.x;}
    public double getY(){return this.y;}
    public void setX(double new_x) { this.x = new_x; }
    public void setY(double new_y) { this.y = new_y; }
}
