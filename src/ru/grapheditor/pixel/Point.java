package ru.grapheditor.pixel;

import java.awt.Color;
import java.awt.Graphics2D;

public class Point extends Pixel {

    public static final int RADIUS = 3;

    public Point(int x, int y, Color color) {
        super(x, y, color);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
    }

    @Override
    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }
}
