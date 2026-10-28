package ru.grapheditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon implements Shape {

    private final List<int[]> vertices = new ArrayList<>();
    private Color baseColor = Color.GRAY;

    public Polygon(int... points) {
        if (points == null || points.length < 6 || points.length % 2 != 0) {
            throw new IllegalArgumentException("Полигон требует не менее трёх вершин (x, y)");
        }
        for (int i = 0; i < points.length; i += 2) {
            vertices.add(new int[]{points[i], points[i + 1]});
        }
    }

    public void addVertex(int x, int y) {
        vertices.add(new int[]{x, y});
    }

    public void setVertex(int index, int x, int y) {
        vertices.get(index)[0] = x;
        vertices.get(index)[1] = y;
    }

    public void setVertices(int[] points) {
        if (points == null || points.length != vertices.size() * 2 || points.length % 2 != 0) {
            throw new IllegalArgumentException("Неверное число координат вершин");
        }
        for (int i = 0; i < points.length; i += 2) {
            vertices.get(i / 2)[0] = points[i];
            vertices.get(i / 2)[1] = points[i + 1];
        }
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public List<int[]> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
    }

    public int[] xArray() {
        int[] xs = new int[vertices.size()];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = vertices.get(i)[0];
        }
        return xs;
    }

    public int[] yArray() {
        int[] ys = new int[vertices.size()];
        for (int i = 0; i < ys.length; i++) {
            ys[i] = vertices.get(i)[1];
        }
        return ys;
    }

    private java.awt.Polygon asAwtPolygon() {
        return new java.awt.Polygon(xArray(), yArray(), vertices.size());
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(baseColor);
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(asAwtPolygon());
    }

    @Override
    public boolean contains(int x, int y) {
        java.awt.Polygon p = asAwtPolygon();
        return p.contains(x, y) || distanceToEdge(p, x, y) <= 3.0;
    }

    @Override
    public void move(int dx, int dy) {
        for (int[] v : vertices) {
            v[0] += dx;
            v[1] += dy;
        }
    }

    private static double distanceToEdge(java.awt.Polygon p, int px, int py) {
        double best = Double.MAX_VALUE;
        int n = p.npoints;
        for (int i = 0; i < n; i++) {
            int x1 = p.xpoints[i];
            int y1 = p.ypoints[i];
            int x2 = p.xpoints[(i + 1) % n];
            int y2 = p.ypoints[(i + 1) % n];
            best = Math.min(best, segmentDistance(px, py, x1, y1, x2, y2));
        }
        return best;
    }

    private static double segmentDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        double t = lenSq == 0 ? 0
                : Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        double cx = x1 + t * dx;
        double cy = y1 + t * dy;
        return Math.hypot(px - cx, py - cy);
    }
}
