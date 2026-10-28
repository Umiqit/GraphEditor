package ru.grapheditor.shape;

import java.awt.Color;
import java.awt.Graphics2D;

public class ShadowDecorator extends ShapeDecorator {

    private final int offsetX;
    private final int offsetY;
    private final Color shadowColor;

    public ShadowDecorator(Shape inner, int offsetX, int offsetY, Color shadowColor) {
        super(inner);
        if (shadowColor == null) {
            throw new IllegalArgumentException("Цвет тени не задан");
        }
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.shadowColor = shadowColor;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    @Override
    public void draw(Graphics2D g) {
        if (inner instanceof Polygon p) {
            int n = p.getVertexCount();
            int[] xs = p.xArray();
            int[] ys = p.yArray();
            int[] sx = new int[n];
            int[] sy = new int[n];
            for (int i = 0; i < n; i++) {
                sx[i] = xs[i] + offsetX;
                sy[i] = ys[i] + offsetY;
            }
            g.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(),
                    shadowColor.getBlue(), 100));
            g.fillPolygon(sx, sy, n);
        }
        inner.draw(g);
    }
}
