package ru.grapheditor.pixel;

import java.awt.Color;
import java.awt.Graphics2D;

public class Circle extends Pixel {

    private int radius;

    public Circle(int x, int y, int radius, Color color) {
        super(x, y, color);
        setRadius(radius);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Радиус должен быть положительным: " + radius);
        }
        this.radius = radius;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public boolean contains(int px, int py) {
        long dx = px - x;
        long dy = py - y;
        return dx * dx + dy * dy <= (long) radius * radius;
    }
}
