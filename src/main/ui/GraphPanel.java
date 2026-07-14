package ui;

import model.Edge;
import model.Graph;
import model.Vertex;
import algorithm.BellmanFordStep;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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

    private BellmanFordStep currentStep;

    private Vertex startVertex;
    private Vertex endVertex;

    private Vertex draggingVertex = null;
    private boolean draggingVertexMode = false;

    private List<Edge> shortestPathEdges;

    private static final double CURVE_OFFSET = 35.0;
    private static final double TEXT_OFFSET = 22.0;

    private static final Color ORANGE_COLOR = new Color(255, 165, 0);
    private static final Color GRAY_COLOR = new Color(192, 192, 192, 180);
    private static final Color PURPLE_COLOR = new Color(138, 43, 226);

    public GraphPanel(Graph graph) {
        this.graph = graph;
        setBackground(Color.WHITE);

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point graphPoint = screenToGraph(e.getX(), e.getY());

                if (e.getButton() == MouseEvent.BUTTON1) {
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON3) {
                    Vertex clicked = findVertexAt(graphPoint.x, graphPoint.y);
                    if (clicked != null) {
                        draggingVertex = clicked;
                        draggingVertexMode = true;
                    } else {
                        dragging = true;
                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dragging = false;
                    draggingVertex = null;
                    draggingVertexMode = false;
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingVertexMode && draggingVertex != null) {
                    Point graphPoint = screenToGraph(e.getX(), e.getY());
                    draggingVertex.setX(graphPoint.x);
                    draggingVertex.setY(graphPoint.y);
                    repaint();
                } else if (dragging) {
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

    public void setShortestPath(List<Vertex> path) {
        this.shortestPathEdges = new ArrayList<>();
        if (path != null) {
            for (int i = 0; i < path.size() - 1; i++) {
                Edge e = graph.getEdgeBetween(path.get(i), path.get(i + 1));
                if (e != null) {
                    shortestPathEdges.add(e);
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        for (Vertex v : graph.getVertices()) {
            for (Edge e : graph.getNeighbors(v)) {
                drawEdge(g2, e);
            }
        }

        if (currentStep != null) {
            Edge processedEdge = currentStep.getProcessedEdge();

            if (processedEdge != null) {
                drawHighlightedEdge(g2, processedEdge, ORANGE_COLOR, (float) (4 / zoom));
            }

            Vertex activeVertex = processedEdge != null ? processedEdge.getFrom() : null;
            Vertex targetVertex = processedEdge != null ? processedEdge.getTo() : null;

            Map<Vertex, Integer> distances = currentStep.getDistancesSnapshot();
            for (Vertex v : graph.getVertices()) {
                int dist = distances.getOrDefault(v, Integer.MAX_VALUE);
                if (dist != 1_000_000_000 && !v.equals(activeVertex) && !v.equals(targetVertex)) {
                    int vx = (int) v.getX();
                    int vy = (int) v.getY();
                    g2.setColor(GRAY_COLOR);
                    g2.fillOval(vx - RADIUS, vy - RADIUS, RADIUS * 2, RADIUS * 2);
                }
            }
        }

        if (shortestPathEdges != null) {
            for (Edge e : shortestPathEdges) {
                drawHighlightedEdge(g2, e, Color.RED, (float) (5 / zoom));
            }
        }

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

            if (currentStep != null) {
                Edge processedEdge = currentStep.getProcessedEdge();
                if (processedEdge != null) {
                    if (v.equals(processedEdge.getFrom())) {
                        g2.setColor(Color.YELLOW);
                        g2.setStroke(new BasicStroke((float) (3 / zoom)));
                        g2.drawOval(x - RADIUS - 2, y - RADIUS - 2, (RADIUS + 2) * 2, (RADIUS + 2) * 2);
                    }
                    if (v.equals(processedEdge.getTo())) {
                        g2.setColor(PURPLE_COLOR);
                        g2.setStroke(new BasicStroke((float) (3 / zoom)));
                        g2.drawOval(x - RADIUS - 2, y - RADIUS - 2, (RADIUS + 2) * 2, (RADIUS + 2) * 2);
                    }
                }
            }
        }

        if (currentStep != null) {
            Map<Vertex, Integer> distances = currentStep.getDistancesSnapshot();
            for (Vertex v : graph.getVertices()) {
                int dist = distances.getOrDefault(v, Integer.MAX_VALUE);
                if (dist != 1_000_000_000) {
                    int vx = (int) v.getX();
                    int vy = (int) v.getY();
                    String distStr = String.valueOf(dist);
                    g2.setColor(new Color(0, 100, 0));
                    g2.setFont(new Font("Arial", Font.PLAIN, (int) (12 / zoom)));
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(distStr, vx - fm2.stringWidth(distStr) / 2, vy - RADIUS - 8);
                }
            }
        }
    }

    public void setStartVertex(Vertex v) { this.startVertex = v; repaint(); }
    public void setEndVertex(Vertex v) { this.endVertex = v; repaint(); }

    public void clearHighlights() {
        this.startVertex = null;
        this.endVertex = null;
        this.currentStep = null;
        this.shortestPathEdges = null;
        repaint();
    }

    public void setCurrentStep(BellmanFordStep step) {
        this.currentStep = step;
        repaint();
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

        drawArrow(g2, (int) endX, (int) endY, angle, Color.DARK_GRAY);

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

        double leftPerpX = -uy;
        double leftPerpY = ux;

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
        drawArrow(g2, (int) endX, (int) endY, tangentAngle, Color.DARK_GRAY);

        Point2D midPoint = evalQuadCurve(curve, 0.5);
        double textX = midPoint.getX() + leftPerpX * TEXT_OFFSET;
        double textY = midPoint.getY() + leftPerpY * TEXT_OFFSET;

        drawWeight(g2, edge.getWeight(), textX, textY);
    }

    private void drawHighlightedEdge(Graphics2D g2, Edge edge, Color color, float strokeWidth) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();
        Edge reverse = graph.getEdgeBetween(to, from);

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

        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth));

        if (reverse != null) {
            double ux = dx / len;
            double uy = dy / len;
            double leftPerpX = -uy;
            double leftPerpY = ux;
            double ctrlX = (x1 + x2) / 2.0 + leftPerpX * CURVE_OFFSET;
            double ctrlY = (y1 + y2) / 2.0 + leftPerpY * CURVE_OFFSET;
            QuadCurve2D curve = new QuadCurve2D.Double(startX, startY, ctrlX, ctrlY, endX, endY);
            g2.draw(curve);
            double tangentAngle = Math.atan2(endY - ctrlY, endX - ctrlX);
            drawArrow(g2, (int) endX, (int) endY, tangentAngle, color);
        } else {
            g2.drawLine((int) startX, (int) startY, (int) endX, (int) endY);
            drawArrow(g2, (int) endX, (int) endY, angle, color);
        }
    }

    private Point2D evalQuadCurve(QuadCurve2D curve, double t) {
        double x = (1-t)*(1-t)*curve.getX1() + 2*(1-t)*t*curve.getCtrlX() + t*t*curve.getX2();
        double y = (1-t)*(1-t)*curve.getY1() + 2*(1-t)*t*curve.getCtrlY() + t*t*curve.getY2();
        return new Point2D.Double(x, y);
    }

    private void drawArrow(Graphics2D g2, int x, int y, double angle, Color color) {
        int arrowSize = (int) (12 / zoom);
        double arrowAngle = Math.toRadians(25);
        int x1 = (int) (x - arrowSize * Math.cos(angle - arrowAngle));
        int y1 = (int) (y - arrowSize * Math.sin(angle - arrowAngle));
        int x2 = (int) (x - arrowSize * Math.cos(angle + arrowAngle));
        int y2 = (int) (y - arrowSize * Math.sin(angle + arrowAngle));
        g2.setColor(color);
        g2.fillPolygon(new int[]{x, x1, x2}, new int[]{y, y1, y2}, 3);
    }

    private void drawWeight(Graphics2D g2, int weight, double x, double y) {
        g2.setColor(Color.RED);
        int fontSize;
        if (zoom <= 1.0) {
            fontSize = (int) (12 + zoom);
        } else {
            fontSize = (int) (13 + 3 * Math.log(zoom));
        }
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        String weightStr = String.valueOf(weight);
        int sw = fm.stringWidth(weightStr);
        int ascent = fm.getAscent();
        g2.drawString(weightStr, (int)(x - sw/2.0), (int)(y + ascent/2.0 - 2));
    }

    private Vertex findVertexAt(int graphX, int graphY) {
        for (Vertex v : graph.getVertices()) {
            int vx = (int) v.getX();
            int vy = (int) v.getY();
            double dist = Math.sqrt((graphX - vx) * (graphX - vx) + (graphY - vy) * (graphY - vy));
            if (dist <= RADIUS) {
                return v;
            }
        }
        return null;
    }
}