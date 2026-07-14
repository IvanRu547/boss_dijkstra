package ui;

import model.Edge;
import model.Graph;
import model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Point2D;

public class GraphPanel extends JPanel {

    private Graph graph;
    private Vertex selectedVertex;
    private static final int RADIUS = 25;

    private double zoom = 1.0;
    private static final double ZOOM_MIN = 0.3;
    private static final double ZOOM_MAX = 3.0;
    private static final double ZOOM_STEP = 0.1;

    private double offsetX = 0;
    private double offsetY = 0;
    private int lastMouseX;
    private int lastMouseY;
    private boolean dragging = false;

    private Vertex startVertex;
    private Vertex endVertex;

    // Размеры в МИРОВЫХ координатах (при зуме 1.0 они соответствуют пикселям)
    private static final double CURVE_OFFSET = 35.0;   // изгиб дуги
    private static final double TEXT_OFFSET = 22.0;    // отступ текста от дуги/прямой

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setBackground(Color.WHITE);

        // Зум колёсиком
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(zoom + ZOOM_STEP, ZOOM_MAX);
                } else {
                    zoom = Math.max(zoom - ZOOM_STEP, ZOOM_MIN);
                }
                int mouseX = e.getX();
                int mouseY = e.getY();
                offsetX = mouseX - (mouseX - offsetX) * zoom / oldZoom;
                offsetY = mouseY - (mouseY - offsetY) * zoom / oldZoom;
                repaint();
            }
        });

        // Перетаскивание холста правой кнопкой
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dragging = true;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dragging = false;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    offsetX += e.getX() - lastMouseX;
                    offsetY += e.getY() - lastMouseY;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        });
    }

    public void setSelectedVertex(Vertex v) {
        this.selectedVertex = v;
    }


    public Point screenToGraph(int screenX, int screenY) {
        int graphX = (int) ((screenX - offsetX) / zoom);
        int graphY = (int) ((screenY - offsetY) / zoom);
        return new Point(graphX, graphY);
    }

    public double getZoom() {
        return zoom;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        // Рёбра
        for (Vertex v : graph.getVertices()) {
            for (Edge e : graph.getNeighbors(v)) {
                drawEdge(g2, e);
            }
        }

        // Вершины
        for (Vertex v : graph.getVertices()) {
            int x = (int) v.getX();
            int y = (int) v.getY();


            if (v.equals(startVertex)) {
                g2.setColor(Color.GREEN);
                g2.fillOval(x - RADIUS - 5, y - RADIUS - 5, (RADIUS + 5) * 2, (RADIUS + 5) * 2);
            }
            if (v.equals(endVertex)) {
                g2.setColor(Color.RED);
                g2.fillOval(x - RADIUS - 5, y - RADIUS - 5, (RADIUS + 5) * 2, (RADIUS + 5) * 2);
            }

            if (v.equals(selectedVertex)) {
                g2.setColor(Color.YELLOW);
                g2.fillOval(x - RADIUS - 5, y - RADIUS - 5, (RADIUS + 5) * 2, (RADIUS + 5) * 2);
            }

            g2.setColor(new Color(100, 149, 237));
            g2.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke((float) (2 / zoom)));
            g2.drawOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);

            g2.setFont(new Font("Arial", Font.BOLD, (int) (16 / zoom)));
            FontMetrics fm = g2.getFontMetrics();
            String label = v.getName();
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + fm.getAscent() / 2 - 2);
        }
    }

    public void setStartVertex(Vertex v) { this.startVertex = v; }
    public void setEndVertex(Vertex v) { this.endVertex = v; }

    public void clearHighlights() {
        this.startVertex = null;
        this.endVertex = null;
        //this.currentStep = null;
        //this.finalPath = null;
    }

    private void drawEdge(Graphics2D g2, Edge edge) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();
        Edge reverse = graph.getEdgeBetween(to, from);

        if (reverse != null) {
            drawCurvedEdge(g2, edge);
        } else {
            drawStraightEdge(g2, edge);
        }
    }

    private void drawStraightEdge(Graphics2D g2, Edge edge) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();

        double x1 = from.getX();
        double y1 = from.getY();
        double x2 = to.getX();
        double y2 = to.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        double angle = Math.atan2(dy, dx);

        double startX = x1 + RADIUS * Math.cos(angle);
        double startY = y1 + RADIUS * Math.sin(angle);
        double endX = x2 - RADIUS * Math.cos(angle);
        double endY = y2 - RADIUS * Math.sin(angle);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke((float) (2 / zoom)));
        g2.drawLine((int) startX, (int) startY, (int) endX, (int) endY);

        drawArrow(g2, (int) endX, (int) endY, angle);

        // Перпендикуляр вправо
        double perpX = dy;
        double perpY = -dx;
        double perpLen = Math.sqrt(perpX * perpX + perpY * perpY);
        if (perpLen > 0) {
            perpX /= perpLen;
            perpY /= perpLen;
        }

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;
        double textX = midX + perpX * TEXT_OFFSET;
        double textY = midY + perpY * TEXT_OFFSET;

        drawWeight(g2, edge.getWeight(), textX, textY);
    }

    private void drawCurvedEdge(Graphics2D g2, Edge edge) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();

        double x1 = from.getX();
        double y1 = from.getY();
        double x2 = to.getX();
        double y2 = to.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;

        double ux = dx / len;
        double uy = dy / len;

        // Левый перпендикуляр (против часовой)
        double leftPerpX = -uy;
        double leftPerpY = ux;

        // Изгиб влево от направления
        double ctrlX = (x1 + x2) / 2.0 + leftPerpX * CURVE_OFFSET;
        double ctrlY = (y1 + y2) / 2.0 + leftPerpY * CURVE_OFFSET;

        double angle = Math.atan2(dy, dx);
        double startX = x1 + RADIUS * Math.cos(angle);
        double startY = y1 + RADIUS * Math.sin(angle);
        double endX = x2 - RADIUS * Math.cos(angle);
        double endY = y2 - RADIUS * Math.sin(angle);

        QuadCurve2D curve = new QuadCurve2D.Double(startX, startY, ctrlX, ctrlY, endX, endY);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke((float) (2 / zoom)));
        g2.draw(curve);

        double tangentAngle = Math.atan2(endY - ctrlY, endX - ctrlX);
        drawArrow(g2, (int) endX, (int) endY, tangentAngle);

        // Текст с той же стороны, что и изгиб (снаружи дуги)
        Point2D midPoint = evalQuadCurve(curve, 0.5);
        double textX = midPoint.getX() + leftPerpX * TEXT_OFFSET;
        double textY = midPoint.getY() + leftPerpY * TEXT_OFFSET;

        drawWeight(g2, edge.getWeight(), textX, textY);
    }

    private Point2D evalQuadCurve(QuadCurve2D curve, double t) {
        double x = (1-t)*(1-t)*curve.getX1() + 2*(1-t)*t*curve.getCtrlX() + t*t*curve.getX2();
        double y = (1-t)*(1-t)*curve.getY1() + 2*(1-t)*t*curve.getCtrlY() + t*t*curve.getY2();
        return new Point2D.Double(x, y);
    }

    private void drawArrow(Graphics2D g2, int x, int y, double angle) {
        int arrowSize = (int) (12 / zoom);  // стрелка всегда 12 экранных пикселей
        double arrowAngle = Math.toRadians(25);
        int x1 = (int) (x - arrowSize * Math.cos(angle - arrowAngle));
        int y1 = (int) (y - arrowSize * Math.sin(angle - arrowAngle));
        int x2 = (int) (x - arrowSize * Math.cos(angle + arrowAngle));
        int y2 = (int) (y - arrowSize * Math.sin(angle + arrowAngle));
        g2.setColor(Color.DARK_GRAY);
        g2.fillPolygon(new int[]{x, x1, x2}, new int[]{y, y1, y2}, 3);
    }

    private void drawWeight(Graphics2D g2, int weight, double x, double y) {
        g2.setColor(Color.RED);
        // Плавное масштабирование шрифта: от 9.36px при zoom=0.3 до 23.4px при zoom=3.0
        int fontSize = (int) (13 * (0.6 + 0.4 * zoom));
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        String weightStr = String.valueOf(weight);
        int sw = fm.stringWidth(weightStr);
        int ascent = fm.getAscent();
        g2.drawString(weightStr, (int)(x - sw/2.0), (int)(y + ascent/2.0 - 2));
    }
}