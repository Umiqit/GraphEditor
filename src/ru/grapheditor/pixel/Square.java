package ru.grapheditor.pixel;

import java.awt.Color;
import java.awt.Graphics2D;

public class Square extends Pixel {

    private int side;

    public Square(int x, int y, int side, Color color) {
        super(x, y, color);
        setSide(side);
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        if (side <= 0) {
            throw new IllegalArgumentException("Сторона должна быть положительной: " + side);
        }
        this.side = side;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect(x - side / 2, y - side / 2, side, side);
    }

    @Override
    public boolean contains(int px, int py) {
        return Math.abs(px - x) <= side / 2 && Math.abs(py - y) <= side / 2;
    }
}
