package ru.grapheditor.shape;

import java.awt.Graphics2D;

public interface Shape {
    void draw(Graphics2D g);

    boolean contains(int x, int y);

    void move(int dx, int dy);
}
