package ru.grapheditor.shape;

import java.awt.Color;
import java.awt.Graphics2D;

public class FillDecorator extends ShapeDecorator {

    private final Color fillColor;

    public FillDecorator(Shape inner, Color fillColor) {
        super(inner);
        if (fillColor == null) {
            throw new IllegalArgumentException("Цвет заливки не задан");
        }
        this.fillColor = fillColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    @Override
    public void draw(Graphics2D g) {
        if (inner instanceof Polygon p) {
            g.setColor(fillColor);
            g.fillPolygon(p.xArray(), p.yArray(), p.getVertexCount());
        }
        inner.draw(g);
    }
}
