package ru.grapheditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class BorderDecorator extends ShapeDecorator {

    private final Color borderColor;
    private final float width;

    public BorderDecorator(Shape inner, Color borderColor, float width) {
        super(inner);
        if (borderColor == null) {
            throw new IllegalArgumentException("Цвет контура не задан");
        }
        if (width <= 0) {
            throw new IllegalArgumentException("Толщина контура должна быть положительной: " + width);
        }
        this.borderColor = borderColor;
        this.width = width;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public float getWidth() {
        return width;
    }

    @Override
    public void draw(Graphics2D g) {
        if (inner instanceof Polygon p) {
            java.awt.Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(width));
            g.setColor(borderColor);
            g.drawPolygon(p.xArray(), p.yArray(), p.getVertexCount());
            g.setStroke(old);
        }
        inner.draw(g);
    }
}
