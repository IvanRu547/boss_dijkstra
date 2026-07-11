package ui;

import model.Edge;
import model.Graph;
import model.Vertex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

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

                // Зумим к точке под курсором
                int mouseX = e.getX();
                int mouseY = e.getY();
                offsetX = mouseX - (mouseX - offsetX) * zoom / oldZoom;
                offsetY = mouseY - (mouseY - offsetY) * zoom / oldZoom;

                repaint();
            }
        });

        // Перетаскивание холста
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

    // Переводит экранные координаты в координаты графа
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

        // Применяем трансформацию
        g2.translate(offsetX, offsetY);
        g2.scale(zoom, zoom);

        // Рёбра
        for (Vertex v : graph.getVertices()) {
            for (Edge e : graph.getNeighbors(v)) {
                int x1 = (int) e.getFrom().getX();
                int y1 = (int) e.getFrom().getY();
                int x2 = (int) e.getTo().getX();
                int y2 = (int) e.getTo().getY();

                double angle = Math.atan2(y2 - y1, x2 - x1);
                int endX = (int) (x2 - RADIUS * Math.cos(angle));
                int endY = (int) (y2 - RADIUS * Math.sin(angle));
                int startX = (int) (x1 + RADIUS * Math.cos(angle));
                int startY = (int) (y1 + RADIUS * Math.sin(angle));

                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke((float) (2 / zoom)));
                g2.drawLine(startX, startY, endX, endY);

                drawArrow(g2, endX, endY, angle);

                int mx = (x1 + x2) / 2;
                int my = (y1 + y2) / 2;
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, (int) (13 / zoom)));
                FontMetrics fm = g2.getFontMetrics();
                String weightStr = String.valueOf(e.getWeight());
                g2.drawString(weightStr, mx - fm.stringWidth(weightStr) / 2, my - (int) (8 / zoom));
            }
        }

        // Вершины
        for (Vertex v : graph.getVertices()) {
            int x = (int) v.getX();
            int y = (int) v.getY();

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

    private void drawArrow(Graphics2D g2, int x, int y, double angle) {
        int arrowSize = (int) (12 / zoom);
        double arrowAngle = Math.toRadians(25);

        int x1 = (int) (x - arrowSize * Math.cos(angle - arrowAngle));
        int y1 = (int) (y - arrowSize * Math.sin(angle - arrowAngle));
        int x2 = (int) (x - arrowSize * Math.cos(angle + arrowAngle));
        int y2 = (int) (y - arrowSize * Math.sin(angle + arrowAngle));

        g2.setColor(Color.DARK_GRAY);
        g2.fillPolygon(new int[]{x, x1, x2}, new int[]{y, y1, y2}, 3);
    }
}