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
        for (int[] v : vertices) {
            g.fillOval(v[0] - 3, v[1] - 3, 6, 6);
        }
    }

    @Override
    public boolean contains(int x, int y) {
        return asAwtPolygon().contains(x, y);
    }
}
